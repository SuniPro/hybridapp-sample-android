package com.hybridApp.sample.webscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.Html
import android.view.*
import android.webkit.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hybridApp.sample.HybridAppConst
import com.hybridApp.sample.BuildConfig
import com.hybridApp.sample.R
import com.hybridApp.sample.base.BaseFragment
import com.hybridApp.sample.databinding.FragmentWebBinding
import com.hybridApp.sample.domain.model.User
import com.hybridApp.sample.expandablemenu.ExpandableMenuContract
import com.hybridApp.sample.expandablemenu.ExpandableMenuViewModel
import com.hybridApp.sample.main.MainActivity
import com.hybridApp.sample.main.MainContract
import com.hybridApp.sample.main.MainViewModel
import com.hybridApp.sample.util.DLog
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URISyntaxException


@AndroidEntryPoint
class WebFragment : BaseFragment<FragmentWebBinding>() {
//BaseFragment<FragmentWebScreenBinding>(FragmentWebScreenBinding::inflate) {

    private val expandableMenuViewModel: ExpandableMenuViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    //private val webViewModel: WebViewModel by activityViewModels()

    private var requestUrl: String? = null
    private var isFullScreen: Boolean = false

    //    private var isClearHistory: Boolean = false
    private var isBackFromWishList: Boolean = false
    private val webView: WebView by lazy {
        binding.webview
    }
    private val refreshLayout: SwipeRefreshLayout by lazy {
        binding.swipeRefreshLayout
    }
    private val progressBar: ContentLoadingProgressBar by lazy {
        binding.progressBar
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWebBinding {
        return FragmentWebBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestUrl = arguments?.getString(KEY_URL, null)
        isFullScreen = arguments?.getBoolean(KEY_IS_FULL_SCREEN, false) == true
        DLog.d("isFullScreen=$isFullScreen, requestUrl=$requestUrl")

        initViews()
        initWebView()
        bindViews()
        initObservers()
    }

    override fun onPause() {
        super.onPause()
        DLog.d("webFragment onPause")
        CookieManager.getInstance().flush()
    }

    override fun onResume() {
        super.onResume()
        DLog.d("webFragment onResume")
        CookieManager.getInstance().flush()
    }

    override fun onStart() {
        super.onStart()
        DLog.d("webFragment onStart")
        CookieManager.getInstance().flush()
    }

    override fun onStop() {
        super.onStop()
        DLog.d("webFragment onStop")
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        super.onDestroy()
        DLog.d("webFragment onDestroy")
        CookieManager.getInstance().flush()
    }

    private fun onLogin(id: String, name: String, membershipLevel: String, resNo: String) {
        mainViewModel.setEvent(MainContract.Event.OnLogin(User(id, name, membershipLevel, resNo)))

    }

    private fun onLogout() {
        mainViewModel.setEvent(MainContract.Event.OnLogout)
    }

    private fun onBasketCount(count: Int) {
        if (isFullScreen) {
            return
        }

        //mainViewModel.setEvent(MainContract.Event.OnBasketCount(count))
        with(binding.layoutNavigationBar) {
            if (count > 0) {
                txtCartItemCount.text = count.toString()
                txtCartItemCount.visibility = View.VISIBLE
            } else {
                txtCartItemCount.visibility = View.GONE
            }
        }
    }

    private fun onPageTitle(title: String?) {
        //mainViewModel.setEvent(MainContract.Event.OnPageTitle(title))
        DLog.d("title=$title")
        if (title == HybridAppConst.PAGE_TITLE_LOGO) {
            // show logo image
            with(binding.layoutNavigationBar) {
                tvTitle.visibility = View.GONE
                ivTitle.visibility = View.VISIBLE
            }
            return
        }
        if (title.isNullOrBlank()) {
            // no title
            with(binding.layoutNavigationBar) {
                tvTitle.visibility = View.GONE
                ivTitle.visibility = View.GONE
            }
        } else {
            // show title text
            with(binding.layoutNavigationBar) {
                ivTitle.visibility = View.GONE
                tvTitle.text = title
                tvTitle.visibility = View.VISIBLE
            }
        }
    }

    private fun onPageMenuIcon(icon: String) {
        if (isFullScreen) {
            return
        }

        mainViewModel.setEvent(MainContract.Event.OnPageMenuIcon(icon))

        DLog.d("menu=$icon")
        when (icon) {
            HybridAppConst.MENU_ICON_BACK -> {
                // show back button
                with(binding.layoutNavigationBar) {
                    llBtnMenu.visibility = View.GONE
                    llBtnBack.visibility = View.VISIBLE
                }
            }
            else -> {
                // show menu button
                with(binding.layoutNavigationBar) {
                    llBtnBack.visibility = View.GONE
                    llBtnMenu.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun onStartFullPagePopup(url: String) {
        mainViewModel.setEvent(MainContract.Event.OnStartFullPagePopup(url))
    }

    private fun onHomePageStarted() {
        if (activity != null) {
            (activity as MainActivity).resetBottomSheet()
        }
    }

    private fun initViews() {
        if (isFullScreen) {
            mainViewModel.setEvent(MainContract.Event.OnPageMenuIcon(HybridAppConst.MENU_ICON_BACK))
            with(binding.layoutNavigationBar) {
                ivTitle.visibility = View.GONE
                llBtnMenu.visibility = View.GONE
                llBtnBack.visibility = View.GONE
                llBtnClose.visibility = View.VISIBLE
                llBasket.visibility = View.GONE
                llPresentBox.visibility = View.GONE
            }
        } else {
            with(binding.layoutNavigationBar) {
                llBtnMenu.visibility = View.VISIBLE
                llBtnBack.visibility = View.GONE
                llBtnClose.visibility = View.GONE
                llBasket.visibility = View.VISIBLE
                llPresentBox.visibility = View.VISIBLE
            }
        }
    }

    private fun initWebView() {
        webView.apply {
            WebView.setWebContentsDebuggingEnabled(true)
            settingCookieManager(this)
            webViewSettings(settings)

            addJavascriptInterface(WebAppInterface(), "AppInterface")

            webViewClient = CustomWebViewClient(false)
            webChromeClient = CustomWebChromeClient()

            if (requestUrl.isNullOrBlank()) {
                loadPage(HybridAppConst.homePage)
            } else {
                loadPage(requestUrl)
            }
        }
        // 앱삭제시 쿠키 삭제
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addDataScheme("package")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                DLog.d("onReceive: $intent")
                webView.clearCache(true)
                webView.clearHistory()
                val cookieManager = CookieManager.getInstance()
                cookieManager.removeAllCookies { value ->
                    DLog.d("onReceive: $value")
                }
                cookieManager.flush()
            }
        }
        context?.registerReceiver(receiver, intentFilter)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewSettings(settings: WebSettings) {
        settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            textZoom = 100
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(true)
            setSupportZoom(true)

            allowFileAccess = true
            defaultTextEncodingName = "utf-8"
            mediaPlaybackRequiresUserGesture = false
        }
    }

    private fun bindViews() {
        refreshLayout.setOnRefreshListener {
            webView.reload()
        }
        with(binding.layoutNavigationBar) {
            btnMenu.setOnClickListener {
                mainViewModel.setEvent(MainContract.Event.OnOpenDrawer(System.currentTimeMillis()))
            }
            btnBack.setOnClickListener {
                goBack()
            }
            btnPresentBox.setOnClickListener {
                //goto present box page
                loadPage(BelleForetConst.presentBox)
            }
            btnBasket.setOnClickListener {
                // goto basket page
                loadPage(BelleForetConst.basketPage)
            }
            btnClose.setOnClickListener {
                processClose()
            }

            llBtnMenu.setOnClickListener {
                mainViewModel.setEvent(MainContract.Event.OnOpenDrawer(System.currentTimeMillis()))
            }
            llBtnBack.setOnClickListener {
                goBack()
            }
            llBasket.setOnClickListener {
                // goto basket page
                loadPage(BelleForetConst.basketPage)
            }
            llBtnClose.setOnClickListener {
                processClose()
            }
            ivTitle.setOnClickListener {
                loadPage(BelleForetConst.homePage)
            }
        }

    }

    private fun processClose() {
        if (activity != null) {
            (activity as MainActivity).closeFullPopup(null)
        }
    }

    private fun processCloseAndPageMove(url: String) {
        if (activity != null) {
            (activity as MainActivity).closeFullPopup(url)
        }
    }

    private fun initObservers() {
        if (requestUrl.isNullOrBlank().not()) { // full screen popup
            return
        }
        lifecycleScope.launchWhenStarted {
            expandableMenuViewModel.uiState.collect {
                when (it) {
                    is ExpandableMenuContract.State.SelectUrl -> {
                        DLog.d("state ==> $it")
                        //isClearHistory = true
                        loadPage(it.msgBox.url)
                    }
                    else -> {}
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            mainViewModel.uiState.collect {
                when (it) {
                    is MainContract.State.BasketCount -> {
                        onBasketCount(it.count)
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mainViewModel.giftCnt.observe(requireActivity()) {
                giftCount(it)
            }

        }
    }

    private fun settingCookieManager(webView: WebView) {
        with(CookieManager.getInstance()) {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
            this.acceptCookie()
        }
    }

//    fun reload() {
//        webView.reload()
//    }

//    fun loadPage(url: String?, isFullScreen: Boolean) {
//        loadPage(url)
//    }

    fun loadPage(url: String?) {
        DLog.d("url=$url")

        if (url.isNullOrBlank()) {
            return
        }

        var uri = url
        if (url.startsWith("/")) {  // '/' 중복 체크 필요
            uri = "${HybridAppConst.baseUrl}$url"
        }
        if (uri.startsWith("http://").not() && uri.startsWith("https://").not()) {
            uri = "http://$uri"
        }
        webView.loadUrl(uri)
    }

    fun currentUrl(): String? {
        return webView.url
    }

    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    private fun giftCount(giftCnt: Int) {
        with(binding.layoutNavigationBar) {
            if (giftCnt > 0) {
                txtPresentBoxItemCount.visibility = View.VISIBLE
                txtPresentBoxItemCount.text = giftCnt.toString()
            } else {
                txtPresentBoxItemCount.visibility = View.GONE
                txtPresentBoxItemCount.text = "0"
            }
        }
    }

    fun goBack(): Boolean {
        DLog.d("goBack - canGoBack=${webView.canGoBack()}, currentUrl=${webView.url}")
        return if (webView.canGoBack()) {
            val uri = Uri.parse(webView.url)
            DLog.d("path=${uri.path}")
            if (uri.path != null && uri.path!!.contains("user/mypage/wishList.do", true)) {
                isBackFromWishList = true
            }
            webView.goBack()
            true
        } else {
            DLog.d("originalUrl=${webView.originalUrl}, url=${webView.url}")
            false
        }
    }

    inner class CustomWebViewClient(
        private val isDialogPopup: Boolean
    ) : WebViewClient() {

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)

            try {
                val jsonRequest = JSONObject()
                request?.let {
                    jsonRequest.put("method", request.method)
                    jsonRequest.put("url", "$requestUrl")
                }
                error?.let {
                    val jsonError = JSONObject()
                    jsonError.put("code", "${error.errorCode}")
                    jsonError.put("description", "${error.description}")

                    val jsonLog = JSONObject()
                    jsonLog.put("request", jsonRequest)
                    jsonLog.put("error", jsonError)

                    Firebase.crashlytics.log(jsonLog.toString())
                    Firebase.crashlytics.recordException(RuntimeException("[${error.errorCode}] ${error.description}"))
                }
                DLog.e("[${error?.errorCode}] ${error?.description}")
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }

            /*error?.let {
                when (error.errorCode) {
                    ERROR_HOST_LOOKUP, ERROR_TIMEOUT, ERROR_CONNECT -> {
                        showErrorPopup(view, true)
                    }
                    else -> {
                        showErrorPopup(view, false)
                    }
                }
            }*/

        }

        private fun showErrorPopup(view: WebView?, isFinish: Boolean, parent: ViewGroup) {
            view?.run {
                val customView = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, parent, false)
                val tvMsg = customView.findViewById<TextView>(R.id.tv_msg)
                val btnOk = customView.findViewById<Button>(R.id.btn_ok)
                val msg = getString(R.string.webview_error_prompt)
                tvMsg.text = Html.fromHtml(msg, FROM_HTML_MODE_LEGACY)
                btnOk.text = getString(android.R.string.ok)

                val builder = AlertDialog.Builder(view.context).apply {
                    setView(customView)
                    setCancelable(false)
                }
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                btnOk.setOnClickListener {
                    dialog.dismiss()
                    if (isFinish) {
                        activity?.finish()
                    }
                }
            }
        }


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            DLog.d("url = $url")

            refreshLayout.isEnabled = false

            if (url.isNullOrBlank()) {
                return
            }

            if (activity != null) {
                if (url.contains("ticket.do")) {
                    (activity as MainActivity).buyTicketSelected()
                } else if (url.contains("myTicket.do")) {
                    (activity as MainActivity).myTicketSelected()
                }
            }
            with(CookieManager.getInstance()) {
                DLog.d("pageStart getCookie = ${this.getCookie(url)}")
            }


            /* 장바구니화면에서 티켓구매 상세화면으로 돌아올 때 reload 요청(from Server part) */
            if (isBackFromWishList) {
                isBackFromWishList = false
                val uri = Uri.parse(url)
                if (uri.path != null && uri.path!!.contains("detail.do", true)) {
                    view?.reload()
                    DLog.d("reload url=$url")
                    return
                }
            }

            /* page load & history back scheme
             *   login/logout
             *   menu/back button, title, basket
             */
            if (url.contains(HybridAppConst.BELLEFORET_SCHEME, true)) {
                val uri = Uri.parse(url)
                uri.host?.let { host ->
                    when (host) {
                        HybridAppConst.SCHEME_HOST_LOGIN -> {
                            val id = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_ID)
                            val name = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_NAME)
                            val membershipLevel =
                                uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_MEMBERSHIP_LEVEL)
                            val resNo = uri.getQueryParameter(HybridAppConst.RES_NO)
                            if (id.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("회원 id 정보가 없습니다.")
                                return
                            }
                            if (name.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("회원명 정보가 없습니다.")
                                return
                            }
                            if (membershipLevel.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("회원등급 정보가 없습니다.")
                                return
                            }
                            if (resNo.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("resNo 정보가 없습니다.")
                                return
                            }
                            onLogin(id, name, membershipLevel, resNo)
                            return
                        }
                        HybridAppConst.SCHEME_HOST_LOGOUT -> {
                            onLogout()
                            return
                        }
                        else -> {}
                    }
                }

                // menu, title, basket
                processMenuTitleBasketScheme(uri)
                return
            }


            if (isDialogPopup) {
                return
            }

            if (isFullScreen.not()) {
                if (url.equals("${HybridAppConst.homePage}/", true) ||
                    url.equals("${HybridAppConst.homePage}/index.do", true)
                ) {
                    onHomePageStarted()
                }
            }
            progressBar.show()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            DLog.d("url = $url")
            with(CookieManager.getInstance()) {
                if (url != null) {
                    DLog.d("pageFinish getCookie = ${this.getCookie(url)}")
                }
            }
            CookieManager.getInstance().flush()
            /*if (isClearHistory) {
                isClearHistory = false
                view?.clearHistory()
                DLog.d("clearHistory")
            }*/
            try {
                if (url != null) {
                    if (url.startsWith(HybridAppConst.homePage, true)) {
                        val uriPath = Uri.parse(url).path

                        uriPath?.let { path ->
                            // 현장대기시간
//                            val waitingPath = Uri.parse(BelleForetConst.waitingTimePage).path
                            // menu - 이벤트
                            val eventPagePath = Uri.parse(HybridAppConst.eventPage).path!!

                            refreshLayout.isEnabled =
//                                path.equals(waitingPath, true) || path.contains(eventPagePath, true)
                                path.contains(eventPagePath, true)

                        }
                    } else {
                        refreshLayout.isEnabled = false
                    }
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }

            if (isDialogPopup) {
                return
            }

            refreshLayout.isRefreshing = false
            progressBar.hide()
            CoroutineScope(Dispatchers.Main).launch {
                val userInfo = mainViewModel.getUserInfo()
                val isLogin = mainViewModel.isLogin()
                if (activity != null && userInfo != null && isLogin) {
                    (activity as MainActivity).menuCount(userInfo.id, userInfo.resNo)
                }else{
                    giftCount(0)
                }
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            val url = request.url.toString().trim()
            DLog.d("isFullScreen=${isFullScreen}, url=$url")

            /* load page scheme
             *   is_full {y, n}
             */
            var belleforetUrl = HybridAppConst.baseUrl
            if (BuildConfig.FLAVOR == "office" || BuildConfig.FLAVOR == "home") {
                belleforetUrl = HybridAppConst.homePage
            }
            val belleforetHost = Uri.parse(belleforetUrl).host

            //scheme://host/path?query
            val uri = Uri.parse(url)

            if (belleforetHost.equals(uri.host, true) && isFullScreen.not()) {
                val isFull = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_IS_FULL)

                isFull?.let {
                    if (it == HybridAppConst.IS_FULL_Y) {
                        view.stopLoading()
                        onStartFullPagePopup(url)
                        return true
                    }
                }
                // 현재 팝업이 아닌 경우  팝업
                // 현재 팝업인 경우
            }


            /* load page  scheme
             *   basketCount, login, logout
             */
            // "belleforetApp://"
            if (url.contains(HybridAppConst.BELLEFORET_SCHEME, true)) {
                uri.host?.let { host ->
                    when (host) {
                        HybridAppConst.SCHEME_HOST_LOGIN -> {
                            val id = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_ID)
                            val name = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_NAME)
                            val membershipLevel =
                                uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_MEMBERSHIP_LEVEL)
                            val resNo = uri.getQueryParameter(HybridAppConst.RES_NO)
                            if (id.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("회원 id 정보가 없습니다.")
                                return true
                            }
                            if (name.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("회원명 정보가 없습니다.")
                                return true
                            }
                            if (membershipLevel.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("회원등급 정보가 없습니다.")
                                return true
                            }
                            if (resNo.isNullOrBlank()) {
                                MainContract.Effect.ShowToast("resNo 정보가 없습니다.")
                                return true
                            }
                            onLogin(id, name, membershipLevel, resNo)
                            return true
                        }
                        HybridAppConst.SCHEME_HOST_LOGOUT -> {
                            onLogout()
                            return true
                        }
                        else -> {}
                    }
                }

                // menu, title, basket
                processMenuTitleBasketScheme(uri)
                return true
            }

            /* 나이스 인증 가이드 - start */
            // 웹뷰 내 표준창에서 외부앱(통신사 인증앱)을 호출하려면 intent:// URI를 별도로 처리해야 함.
            DLog.d("uri.scheme = ${uri.scheme}")
            //if (url.startsWith("intent:")) {
            if ("intent" == uri.scheme) {
                var intent: Intent? = null
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    intent?.let {
                        // 외부앱실행
                        startActivity(it)
                    }
                } catch (e: URISyntaxException) {
                    // URI 문법 오류 처리
                    Firebase.crashlytics.recordException(e)
                } catch (e: ActivityNotFoundException) {
                    val packageName = intent?.`package`
                    gotoGoogleMarket(packageName)
                }
                return true
            }
            //결제시 외부앱 호출 (네이버페이, 보안앱 등)
            else if (url.startsWith("intent:")
                || url.contains("market://")
                || url.contains("vguard")
                || url.contains("droidxantivirus")
                || url.contains("v3mobile")
                || url.contains(".apk")
                || url.contains("mvaccine")
                || url.contains("smartwall://")
                || url.contains("nidlogin://")
                || url.contains("http://m.ahnlab.com/kr/site/download")
            ) {

                var intent: Intent? = null
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    val uri = Uri.parse(intent?.dataString)
                    intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                } catch (e: URISyntaxException) {
                    // URI 문법 오류 처리
                    Firebase.crashlytics.recordException(e)
                } catch (e: ActivityNotFoundException) {
                    val packageName = intent?.`package`
                    gotoGoogleMarket(packageName)
                }
                return true
            } else if (url.startsWith("https://play.google.com/store/apps/details?id=")
                || url.startsWith("marget://details?id=")
            ) {
                // 표준창 내 앱설치하기 버튼 클릭 시 PlayStore 앱으로 연결하기 위한 로직
                val uri = Uri.parse(url)
                val packageName = uri.getQueryParameter("id")
                gotoGoogleMarket(packageName)
                return true
            }
            /* 나이스 인증 가이드 - end */

            DLog.d("loadUrl=$url")
            view.loadUrl(url)
            return true
        }

        private fun gotoGoogleMarket(packageName: String?) {
            try {
                if (packageName.isNullOrBlank().not()) {
                    // 구글마켓 이동
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                    )
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
        }

        private fun processMenuTitleBasketScheme(uri: Uri) {
            try {
                val menuIcon = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_MENU)
                val title = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_TITLE)
                val basketCnt = uri.getQueryParameter(HybridAppConst.SCHEME_PARAM_BASKET)

                DLog.d("menu=$menuIcon, title=$title, basket=$basketCnt, query=${uri.query}")
                if (basketCnt != null && basketCnt.isNotBlank()) {
                    try {
                        mainViewModel.setEvent(MainContract.Event.OnBasketCount(basketCnt.toInt()))
                    } catch (e: Exception) {
                        Firebase.crashlytics.recordException(e)
                    }
                }

                if (menuIcon.isNullOrBlank().not()) {
                    onPageMenuIcon(menuIcon!!)
                }

                if (title != null) {
                    onPageTitle(title)
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    inner class CustomWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            try {
                progressBar.progress = newProgress
                if (newProgress > 99) {
                    progressBar.hide()
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
        }

        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            DLog.d("isDialog=$isDialog, isUserGesture=$isUserGesture")

            val webViewPopup = WebView(view.context)
            settingCookieManager(webViewPopup)
            webViewSettings(webViewPopup.settings)

            webViewPopup.webViewClient = CustomWebViewClient(true)

            val dialogPopup =
                Dialog(
                    view.context,
                    android.R.style.Theme_Translucent_NoTitleBar
                ).apply {
                    setContentView(webViewPopup)

                    window?.apply {
                        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        statusBarColor = context.getColor(android.R.color.transparent)
                    }

                    setOnDismissListener {
//                        CookieManager.getInstance().flush()
                        webViewPopup.destroy()
                    }
                }

            webViewPopup.webChromeClient = object : WebChromeClient() {
                override fun onCloseWindow(window: WebView) {
//                    CookieManager.getInstance().flush()
                    dialogPopup.dismiss()
                    ////window.destroy()
                }
            }

            //TODO 운영배포시 변경필요
            WebView.setWebContentsDebuggingEnabled(true)

            dialogPopup.show()
            dialogPopup.setOnKeyListener { dialog, keyCode, _ ->
                DLog.d("keyCode=$keyCode")
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webViewPopup.canGoBack()) {
                        webViewPopup.goBack()
                    } else {
//                        CookieManager.getInstance().flush()
                        dialog.dismiss()
                        //webViewPopup.loadUrl("javascript:self.close();")
                    }
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }

            resultMsg?.let {
                val transport = it.obj as WebView.WebViewTransport
                transport.webView = webViewPopup
                it.sendToTarget()
            }

            return true
        }

        override fun onCloseWindow(window: WebView) {
            DLog.d("onCloseWindow")
            CookieManager.getInstance().flush()
            window.visibility = View.GONE
            window.destroy()

            super.onCloseWindow(window)
        }

        @SuppressLint("InflateParams")
        override fun onJsAlert(
            view: WebView,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            /*AlertDialog.Builder(view.context).apply {
                setMessage(message)
                setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    result?.confirm()
                }
                setCancelable(false)
                create()
                show()
            }*/
            try {
                val customView = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
                val tvMsg = customView.findViewById<TextView>(R.id.tv_msg)
                val btnOk = customView.findViewById<Button>(R.id.btn_ok)
                tvMsg.text = message
                btnOk.text = getString(android.R.string.ok)

                val builder = AlertDialog.Builder(view.context).apply {
                    setView(customView)
                    setCancelable(false)
                }
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                btnOk.setOnClickListener {
                    result?.confirm()
                    dialog.dismiss()
                }

            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
            return true

        }

        @SuppressLint("InflateParams")
        override fun onJsConfirm(
            view: WebView,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            /*AlertDialog.Builder(view.context).apply {
                setMessage(message)
                setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    result?.confirm()
                }
                setNegativeButton(
                    android.R.string.cancel
                ) { _, _ ->
                    result?.cancel()
                }
                setCancelable(false)
                create()
                show()
            }*/
            try {
                val customView = layoutInflater.inflate(R.layout.layout_custom_confirm_dialog, null)
                val tvMsg = customView.findViewById<TextView>(R.id.tv_msg)
                val btnCancel = customView.findViewById<Button>(R.id.btn_cancel)
                val btnOk = customView.findViewById<Button>(R.id.btn_ok)
                tvMsg.text = message
                btnCancel.text = getString(android.R.string.cancel)
                btnOk.text = getString(android.R.string.ok)

                val builder = AlertDialog.Builder(view.context).apply {
                    setView(customView)
                    setCancelable(false)
                }
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                btnCancel.setOnClickListener {
                    result?.cancel()
                    dialog.dismiss()
                }
                btnOk.setOnClickListener {
                    result?.confirm()
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }

            return true


        }

    }

    inner class WebAppInterface {
//        var payCompleteMovePage = false //결제완료에서 페이지 이동 여부

        /**
         * android 버전
         */
        @JavascriptInterface
        fun getVersion() {
            activity?.runOnUiThread {
                DLog.d("")

                webView.let {
                    val versionName = BuildConfig.VERSION_NAME.split("-")
                    val appVersion = versionName[0]
                    val url = String.format("javascript:setVersion('%s')", appVersion)
                    DLog.d(url)

                    webView.loadUrl(url)
                }
            }
        }

        /**
         * 풀스크린 팝업 닫기
         */
        @JavascriptInterface
        fun fullScreenClose(url: String) {
            activity?.runOnUiThread {
                processCloseAndPageMove(url)
            }
        }
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_IS_FULL_SCREEN = "isFullScreen"
    }
}