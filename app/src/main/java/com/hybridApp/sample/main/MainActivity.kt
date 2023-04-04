package com.hybridApp.sample.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.hybridApp.sample.HybridAppConst
import com.hybridApp.sample.R
import com.hybridApp.sample.base.BaseActivity
import com.hybridApp.sample.databinding.ActivityMainBinding
import com.hybridApp.sample.domain.model.User
import com.hybridApp.sample.expandablemenu.ExpandableMenuContract
import com.hybridApp.sample.expandablemenu.ExpandableMenuViewModel
import com.hybridApp.sample.util.ApiUtil.Companion.requestMenuCount
import com.hybridApp.sample.util.ApiUtil.Companion.send
import com.hybridApp.sample.util.DLog
import com.hybridApp.sample.weather.WeatherContract
import com.hybridApp.sample.weather.WeatherViewModel
import com.hybridApp.sample.webscreen.WebFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import org.json.JSONObject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>({
    ActivityMainBinding.inflate(it)
}) {

    private var isLogin = false
    private var backKeyPressedTime: Long = 0
    private lateinit var toast: Toast
    private var myTicketCount = 0
    private var myCondoCount = 0
    private var myGolfCount = 0
    private val mainViewModel: MainViewModel by viewModels()
    private val expandableMenuViewModel: ExpandableMenuViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()

    private val dlMain: DrawerLayout by lazy {
        binding.dlMain
    }
    private val tvLogin: TextView by lazy {
        binding.layoutNav.tvLogin
    }
    private val tvMyPage: TextView by lazy {
        binding.layoutNav.tvMyPage
    }
    private val tvLoginGrade: TextView by lazy {
        binding.layoutNav.tvLoginGrade
    }
    private val flWebPopupContainer: FrameLayout by lazy {
        binding.layoutMain.flWebPopupContainer
    }
    private var mainWebFragment: WebFragment? = null
    private var waitingTimeWebFragment: WebFragment? = null
    private var fullScreenWebFragment: WebFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        // android 8.0 미만 사용 금지
        showVersionPolicy()

        // 현재 app버전이 최신버전인지 체크
        CoroutineScope(Dispatchers.Default).launch {
            appVersionCheck()
        }
        bindViews()
        initObservers()
        initViews()

        // 개인정보수집알림(구글정책)
        checkPrivacyPolicy()
    }

    @SuppressLint("InflateParams")
    private suspend fun appVersionCheck() {

        val response: Response = send("/api/update_check/android.ajax", "get", true, null) ?: return

        //api 에러시 실행x

        val json = JSONObject(response.body!!.string())
        //app버전이 최신버전이면 그대로 진행
        val pi = packageManager.getPackageInfo(packageName, 0)

        DLog.i("최신버전 : " + json.getString("version"))
        DLog.i("android버전 : " + pi.versionName)
        DLog.i("강제 업데이트 여부 : " + json.getString("force_update"))
        if (json.getString("version").equals(pi.versionName.split("-")[0])) {
            return
        } else { //최신버전이 아닌경우 강제 또는 선택업데이트 팝업창 보여주기
            if (json.getString("force_update").equals("true")) {
                DLog.i("필수 업데이트")
                val customView = layoutInflater.inflate(R.layout.layout_custom_update_alert_dialog, null)
                val btnOk = customView.findViewById<Button>(R.id.btn_ok)
                val builder = AlertDialog.Builder(this).apply {
                    setView(customView)
                    setCancelable(false)
                }
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                btnOk.setOnClickListener {
                    dialog.dismiss()
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                    )
                    finish()
                }
            } else {
                DLog.i("선택 업데이트")
                val customView = layoutInflater.inflate(R.layout.layout_custom_update_confirm_dialog, null)
                val btnOk = customView.findViewById<Button>(R.id.btn_ok)
                val btnCancel = customView.findViewById<Button>(R.id.btn_cancel)
                val builder = AlertDialog.Builder(this).apply {
                    setView(customView)
                    setCancelable(false)
                }
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
                btnOk.setOnClickListener {
                    dialog.dismiss()
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                    )
                    finish()
                }
            }
        }
    }

    suspend fun menuCount(userId: String?, resNo: String?) {
        if (userId.isNullOrBlank() || resNo.isNullOrBlank()) {
            return
        }
        /* menuView */
        val nav = binding.dlMain.findViewById(R.id.layout_nav) as NavigationView
        val navTxtMyCondoCount = nav.findViewById(R.id.txt_condo_count) as TextView
        val navTxtMyGolfCount = nav.findViewById(R.id.txt_golf_count) as TextView

        val json: JSONObject = try {
            //api 호출
            requestMenuCount("/api/user/$userId/$resNo/menuCount.ajax")?.let { JSONObject(it) }
        } catch (e: Exception) {
            DLog.e(e.printStackTrace().toString())
            null
        } ?: return
        myTicketCount = json.getInt("myTicketCnt")
        myCondoCount = json.getInt("myCondoCnt")
        myGolfCount = json.getInt("myGolfCnt")
        mainViewModel.giftCnt.value = json.getInt("giftCnt")
        with(binding.layoutMain.layoutTabBar) {
            if (myTicketCount > 0) {
                txtMyTicketCount.visibility = View.VISIBLE
                txtMyTicketCount.text = myTicketCount.toString()
//                navTxtMyTicketCount.text = myTicketCount.toString()
//                navTxtMyTicketCount.visibility = View.VISIBLE
            } else {
                txtMyTicketCount.visibility = View.GONE
                txtMyTicketCount.text = "0"
//                navTxtMyTicketCount.visibility = View.GONE
//                navTxtMyTicketCount.text = "0"
            }
            if (myCondoCount > 0) {
                navTxtMyCondoCount.visibility = View.VISIBLE
                navTxtMyCondoCount.text = myCondoCount.toString()
            } else {
                navTxtMyCondoCount.visibility = View.GONE
                navTxtMyCondoCount.text = "0"
            }
            if (myGolfCount > 0) {
                navTxtMyGolfCount.visibility = View.VISIBLE
                navTxtMyGolfCount.text = myGolfCount.toString()
            } else {
                navTxtMyGolfCount.visibility = View.GONE
                navTxtMyGolfCount.text = "0"
            }
        }


    }

    @SuppressLint("ObsoleteSdkInt", "InflateParams")
    private fun showVersionPolicy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return
        }
        val customView = layoutInflater.inflate(R.layout.layout_custom_dialog, null)
        val tvTitle = customView.findViewById<TextView>(R.id.tv_title)
        val tvMsg = customView.findViewById<TextView>(R.id.tv_msg)
        val btnOk = customView.findViewById<Button>(R.id.btn_ok)
        tvTitle.text = getString(R.string.notice)
        tvMsg.text = getString(R.string.version_policy_prompt)
        btnOk.text = getString(android.R.string.ok)

        val builder = AlertDialog.Builder(this).apply {
            setView(customView)
            setCancelable(false)
        }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        btnOk.setOnClickListener {
            mainViewModel.onCheckedPrivacyPolicy()
            dialog.dismiss()
            finish()
        }
    }

    private fun checkPrivacyPolicy() {
        // 앱 설치 후 첫 실행 시 개인정보 수집 알림창 표시
        if (mainViewModel.isCheckedPrivacyPolicy()) {
            return
        }

        showPrivacyPolicy()
    }

    @SuppressLint("InflateParams")
    private fun showPrivacyPolicy() {
        val customView = layoutInflater.inflate(R.layout.layout_custom_dialog, null)
        val tvTitle = customView.findViewById<TextView>(R.id.tv_title)
        val tvMsg = customView.findViewById<TextView>(R.id.tv_msg)
        val btnOk = customView.findViewById<Button>(R.id.btn_ok)
        tvTitle.text = getString(R.string.notice)
        tvMsg.text = getString(R.string.privacy_policy_prompt)
        btnOk.text = getString(android.R.string.ok)

        val builder = AlertDialog.Builder(this).apply {
            setView(customView)
            setCancelable(false)
        }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        btnOk.setOnClickListener {
            mainViewModel.onCheckedPrivacyPolicy()
            dialog.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        DLog.d("mainActivity onStart")
    }

    override fun onPause() {
        super.onPause()
        DLog.d("mainActivity onPause")
    }

    override fun onResume() {
        super.onResume()
        DLog.d("mainActivity onResume")
    }

    override fun onStop() {
        DLog.d("isLogin=$isLogin")
        DLog.d("mainActivity onStop")
        super.onStop()
    }

    override fun onDestroy() {
        DLog.d("isLogin=${mainViewModel.isLogin()}")
        DLog.d("mainActivity onDestroy")
        super.onDestroy()

    }

    override fun onBackPressed() {
        if (dlMain.isDrawerOpen(GravityCompat.START)) {
            dlMain.closeDrawer(GravityCompat.START)
            return
        }

        if (flWebPopupContainer.visibility == View.VISIBLE) {
            waitingTimeWebFragment?.let { fragment ->
                if (fragment.canGoBack()) {
                    fragment.goBack()
                } else {
                    DLog.d("waitingTimeWebFragment")
                    flWebPopupContainer.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .apply {
                            remove(fragment)
                        }.commit()
                    waitingTimeWebFragment = null
                }
            }
            fullScreenWebFragment?.let { fragment ->
                if (fragment.canGoBack()) {
                    fragment.goBack()
                } else {
                    DLog.d("fullScreenWebFragment")
                    flWebPopupContainer.visibility = View.GONE
                    supportFragmentManager.beginTransaction()
                        .apply {
                            remove(fragment)
                        }.commit()
                    fullScreenWebFragment = null

                    //DLog.d("mainWebFragment - url=${mainWebFragment?.currentUrl()}")
                    //mainWebFragment?.reload()
                }
            }

            return
        }

        if (mainWebFragment == null) {
            finishApp()
            return
        }

        // 홈 화면이면 finishApp
        val url = mainWebFragment!!.currentUrl()
        if (HybridAppConst.homePage.equals(url, true)
            || HybridAppConst.homePage2.equals(url, true)
            || HybridAppConst.homePage3.equals(url, true)
        ) {
            finishApp()
            return
        }

        if (mainWebFragment != null && mainWebFragment!!.canGoBack()) {
            mainWebFragment!!.goBack()
        } else {
            finishApp()
            return
        }
    }

    private fun finishApp() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            toast = Toast.makeText(
                this,
                getString(R.string.back_pressed_prompt),
                Toast.LENGTH_SHORT
            )
            toast.show()
        } else {
            toast.cancel()
            finish()
        }
    }

    fun closeFullPopup(url: String?) {
        if (flWebPopupContainer.visibility == View.VISIBLE) {
            waitingTimeWebFragment?.let { fragment ->
                DLog.d("waitingTimeWebFragment")
                flWebPopupContainer.visibility = View.GONE
                supportFragmentManager.beginTransaction()
                    .apply {
                        remove(fragment)
                    }.commit()
                waitingTimeWebFragment = null
            }

            fullScreenWebFragment?.let { fragment ->
                DLog.d("fullScreenWebFragment")
                flWebPopupContainer.visibility = View.GONE
                supportFragmentManager.beginTransaction()
                    .apply {
                        remove(fragment)
                    }.commit()
                fullScreenWebFragment = null
                //닫은 후 페이지 이동
                if (url != null) {
                    mainWebFragment?.loadPage(url)
                }
            }
        }
    }

    fun buyTicketSelected() {
        // change button color
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtMyTicket, false)
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtBuyTicket, true)
    }

    fun myTicketSelected() {
        // change button color
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtMyTicket, true)
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtBuyTicket, false)
    }

    private fun createWebFragment(url: String?, isFullScreen: Boolean): WebFragment {
        val fragment = WebFragment()
        val bundle = Bundle()
        bundle.putString(WebFragment.KEY_URL, url)
        bundle.putBoolean(WebFragment.KEY_IS_FULL_SCREEN, isFullScreen)
        fragment.arguments = bundle

        DLog.d("isFullScreen=$isFullScreen, url=$url")
        return fragment
    }

    private fun initViews() {
        showMainWebFragment()
        //mainViewModel.setEvent(MainContract.Event.LoadLoginState)
    }

    private fun bindViews() = with(binding) {
        layoutNav.llDrawerClose.setOnClickListener {
            closeDrawer()
        }
        layoutNav.btnClose.setOnClickListener {
            closeDrawer()
        }
        layoutNav.btnMyPage.setOnClickListener {
            closeDrawer()
            onMyPageClicked()
        }
        tvLogin.setOnClickListener {
            onLoginTextClicked()
        }
        tvMyPage.setOnClickListener {
            onLoginTextClicked()
        }
        layoutNav.llGolfList.setOnClickListener {
            closeDrawer()
            onGolfListClicked()
        }
        layoutNav.llCondoList.setOnClickListener {
            closeDrawer()
            onCondoListClicked()
        }
        layoutNav.llMyTicket.setOnClickListener {
            closeDrawer()
            onTicketClicked()
        }
        layoutMain.layoutTabBar.clBuyTicket.setOnClickListener {
            onBuyTicketClicked()
        }
        layoutMain.layoutTabBar.clMyTicket.setOnClickListener {
            onMyTicketClicked()
        }
        layoutMain.clWaitingTime.setOnClickListener {
            // full-page popup (waiting time page)
            showWaitingPopupWebFragment()
        }
        dlMain.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                weatherViewModel.setEvent(WeatherContract.Event.Load)
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun closeDrawer() {
        dlMain.closeDrawer(GravityCompat.START)
    }

    private fun onGolfListClicked() {
        // goto golfResList page
        mainWebFragment?.loadPage(HybridAppConst.golfResListPage)
    }

    private fun onCondoListClicked() {
        // goto condoResList page
        mainWebFragment?.loadPage(HybridAppConst.condoResListPage)
    }

    private fun onTicketClicked() {
        // goto Ticket page
        mainWebFragment?.loadPage(HybridAppConst.ticketPage)
    }

    private fun onMyTicketClicked() {
        /*if (isLogin.not()) {s
            // goto Login page
            gotoLoginPage()
            return
        }*/

        // goto My Ticket page
        mainWebFragment?.loadPage(HybridAppConst.myTicketPage)

        // change button color
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtBuyTicket, false)
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtMyTicket, true)

    }

    private fun onBuyTicketClicked() {
        /*if (isLogin.not()) {
            // goto Login page
            gotoLoginPage()
            return
        }*/

        // goto Buy Ticket page
        mainWebFragment?.loadPage(HybridAppConst.buyTicketPage)

        buyTicketSelected()
    }

    private fun tabBarButtonUnselected() {
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtMyTicket, false)
        tabBarButtonSelected(binding.layoutMain.layoutTabBar.txtBuyTicket, false)
    }

    private fun tabBarButtonSelected(textView: TextView, isSelected: Boolean) {
        // unselected
        if (isSelected.not()) {
            textView.setTextColor(getColor(baseContext, R.color.unselected_tab_button_color))
            if (textView == binding.layoutMain.layoutTabBar.txtBuyTicket) {
                textView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.icon_store_line,
                    0,
                    0
                )
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.icon_ticket_line,
                    0,
                    0
                )
            }
            return
        }

        // selected
        textView.setTextColor(getColor(baseContext, R.color.selected_tab_button_color))
        if (textView == binding.layoutMain.layoutTabBar.txtBuyTicket) {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.icon_store_filled,
                0,
                0
            )
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.icon_ticket_filled,
                0,
                0
            )
        }

    }

    private fun onLoginTextClicked() {
        //if (mainViewModel.isLogin()) {
        if (isLogin) {
            // goto My page
            mainWebFragment?.loadPage(HybridAppConst.myPage)
        } else {
            // goto Login page
            gotoLoginPage()
        }

        closeDrawer()
    }

    private fun onMyPageClicked() {
        //if (mainViewModel.isLogin().not()) {
        if (isLogin.not()) {
            // goto Login page
            gotoLoginPage()
            return
        }

        // goto My page
        mainWebFragment?.loadPage(HybridAppConst.myPage)
    }

    private fun gotoLoginPage() {
        mainWebFragment?.loadPage(HybridAppConst.loginPage)
    }

    private fun initObservers() {
        lifecycleScope.launchWhenStarted {
            expandableMenuViewModel.uiState.collect {
                when (it) {
                    is ExpandableMenuContract.State.SelectUrl -> {
                        DLog.d("state ==> $it")
                        closeDrawer()
                        tabBarButtonUnselected()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            mainViewModel.uiState.collect {
                DLog.d("state > $it")
                when (it) {
                    is MainContract.State.OpenDrawer -> {
                        openDrawer()
                    }
                    is MainContract.State.Login -> {
                        isLogin = true
                        updateLoginViews(it.user)
                    }
                    is MainContract.State.Logout -> {
                        isLogin = false
                        updateLoginViews(null)
                    }
                    is MainContract.State.PageMenuIcon -> {
                        updatePageMenuIcon(it.icon)
                    }
                    is MainContract.State.StartFullPagePopup -> {
                        startFullScreenPopup(it.url)
                    }
                    else -> {}
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            mainViewModel.effect.collect {
                when (it) {
                    is MainContract.Effect.ShowToast -> {
                        Toast.makeText(this@MainActivity, it.msg, Toast.LENGTH_SHORT).show()
                    }
                    is MainContract.Effect.Exception -> {
                        Firebase.crashlytics.recordException(it.err)
                    }
                }
            }
        }
    }

    private fun openDrawer() {
        dlMain.openDrawer(GravityCompat.START)
    }

    private fun startFullScreenPopup(url: String) {
        DLog.d("url=$url")

        if (fullScreenWebFragment == null) {
            showFullPagePopup(url)
        } else {
            fullScreenWebFragment!!.loadPage(url)
        }
    }

    private fun updatePageMenuIcon(icon: String) {
        DLog.d("menu=$icon")
        when (icon) {
            HybridAppConst.MENU_ICON_BACK -> {
                /* slide menu lock */
                dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            else -> {
                /* slide menu unlock */
                dlMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private fun updateLoginViews(user: User?) {
        DLog.d("user=$user")
        val nav = binding.dlMain.findViewById(R.id.layout_nav) as NavigationView
        val navTxtMyCondoCount = nav.findViewById(R.id.txt_condo_count) as TextView
        val navTxtMyGolfCount = nav.findViewById(R.id.txt_golf_count) as TextView
        if (user == null) {
            val styledText = SpannableString(getString(R.string.login_prompt))
            styledText.setSpan(
                UnderlineSpan(),
                0,
                styledText.lastIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvLogin.text = styledText
            tvLoginGrade.visibility = View.GONE
            tvMyPage.visibility = View.GONE
            navTxtMyGolfCount.visibility = View.GONE
            navTxtMyGolfCount.text = "0"
            navTxtMyCondoCount.visibility = View.GONE
            navTxtMyCondoCount.text = "0"
            with(binding.layoutMain.layoutTabBar) {
                txtMyTicketCount.visibility = View.GONE
                txtMyTicketCount.text = "0"
            }
            return
        }

        tvLogin.text = getString(R.string.login_user_name, user.name)

        val styledText = SpannableString(getString(R.string.login_grade, user.membershipLevel))
        val startIndex = styledText.indexOf(user.membershipLevel)
        val endIndex = startIndex + user.membershipLevel.length

        val membershipColor = with(user.membershipLevel) {
            when {
                contains(HybridAppConst.MEMBERSHIP_GREEN) -> {
                    getColor(baseContext, R.color.membership_green)
                }
                contains(HybridAppConst.MEMBERSHIP_GREEN_EN) -> {
                    getColor(baseContext, R.color.membership_green)
                }
                contains(HybridAppConst.MEMBERSHIP_RED) -> {
                    getColor(baseContext, R.color.membership_red)
                }
                contains(HybridAppConst.MEMBERSHIP_RED_EN) -> {
                    getColor(baseContext, R.color.membership_red)
                }
                else -> {
                    getColor(baseContext, R.color.membership_online)
                }
            }
        }
        /*val membershipColor = when (user.membershipLevel) {

            BelleForetConst.MEMBERSHIP_GREEN -> {
                resources.getColor(R.color.membership_green)
            }
            else -> {
                resources.getColor(R.color.membership_red)
            }
        }*/
        //Color.parseColor("#112233")

        styledText.setSpan(
            //ForegroundColorSpan(Color.RED),
            ForegroundColorSpan(membershipColor),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        styledText.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvLoginGrade.text = styledText
        tvLoginGrade.visibility = View.VISIBLE
        tvMyPage.visibility = View.VISIBLE
    }

    private fun showMainWebFragment() {
        DLog.d("")
        val fragment =
            supportFragmentManager.findFragmentByTag(MAIN_WEB_FRAGMENT) as WebFragment?
                ?: createWebFragment(null, false)
        mainWebFragment = fragment

        supportFragmentManager.beginTransaction()
            .apply {
                replace(binding.layoutMain.flWebContainer.id, fragment, MAIN_WEB_FRAGMENT)
            }.commit()
    }

    private fun showWaitingPopupWebFragment() {
        DLog.d("")

        val fragment =
            supportFragmentManager.findFragmentByTag(WAITING_WEB_FRAGMENT) as WebFragment?
                ?: createWebFragment(HybridAppConst.waitingTimePage, true)
        waitingTimeWebFragment = fragment

        supportFragmentManager.beginTransaction()
            .apply {
                replace(binding.layoutMain.flWebPopupContainer.id, fragment, WAITING_WEB_FRAGMENT)
            }.commit()

        flWebPopupContainer.visibility = View.VISIBLE
    }

    private fun showFullPagePopup(url: String) {
        DLog.d("url=$url")

        val fragment =
            supportFragmentManager.findFragmentByTag(FULL_SCREEN_WEB_FRAGMENT) as WebFragment?
                ?: createWebFragment(url, true)
        fullScreenWebFragment = fragment

        supportFragmentManager.beginTransaction()
            .apply {
                replace(
                    binding.layoutMain.flWebPopupContainer.id,
                    fragment,
                    FULL_SCREEN_WEB_FRAGMENT
                )
            }.commit()

        flWebPopupContainer.visibility = View.VISIBLE
    }

    fun resetBottomSheet() {
        with(binding.layoutMain.layoutTabBar) {
            txtBuyTicket.setTextColor(getColor(baseContext, R.color.unselected_tab_button_color))
            txtBuyTicket.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.icon_store_line,
                0,
                0
            )
            txtMyTicket.setTextColor(getColor(baseContext, R.color.unselected_tab_button_color))
            txtMyTicket.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.icon_ticket_line,
                0,
                0
            )
        }
    }

    companion object {
        const val MAIN_WEB_FRAGMENT = "mainWebFragment"
        const val WAITING_WEB_FRAGMENT = "waitingWebFragment"
        const val FULL_SCREEN_WEB_FRAGMENT = "fullScreenWebFragment"
    }
}