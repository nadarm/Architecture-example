package com.nadarm.boardmvvmrx.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nadarm.boardmvvmrx.AppModule
import com.nadarm.boardmvvmrx.DaggerAppComponent
import com.nadarm.boardmvvmrx.data.DataSourceModule
import com.nadarm.boardmvvmrx.domain.model.Article
import com.nadarm.boardmvvmrx.domain.useCase.GetArticles
import com.nadarm.boardmvvmrx.presentation.view.adapter.ArticleAdapter
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface ListViewModel {
    interface Inputs : ArticleAdapter.Delegate {
        fun newArticleClicked()
    }

    interface Outputs {
        fun articles(): Flowable<List<Article>>
        fun startDetailActivity(): Observable<Article>
        fun startNewArticleActivity(): Observable<Boolean>
    }

    class ViewModel(application: Application) : AndroidViewModel(application), Inputs, Outputs {
        @Inject
        lateinit var getArticlesUseCase: GetArticles

        private val articleClicked: PublishSubject<Article> = PublishSubject.create()
        private val newArticleClicked: PublishSubject<Boolean> = PublishSubject.create()

        private val articles: Flowable<List<Article>>
        private val startDetailActivity: Observable<Article> =
            articleClicked.throttleFirst(500, TimeUnit.MILLISECONDS)
        private val startNewArticleActivity: Observable<Boolean> =
            newArticleClicked.throttleFirst(500, TimeUnit.MILLISECONDS)

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .dataSourceModule(DataSourceModule())
                .build()
                .inject(this)

            this.articles = this.getArticlesUseCase.execute(Unit)
        }

        override fun articles(): Flowable<List<Article>> = this.articles
        override fun startDetailActivity(): Observable<Article> = this.startDetailActivity
        override fun startNewArticleActivity(): Observable<Boolean> = this.startNewArticleActivity

        override fun newArticleClicked() {
            this.newArticleClicked.onNext(true)
        }

        override fun articleClicked(article: Article) {
            this.articleClicked.onNext(article)
        }

    }
}