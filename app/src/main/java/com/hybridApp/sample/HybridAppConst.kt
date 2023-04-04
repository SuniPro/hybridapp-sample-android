package com.hybridApp.sample

import com.hybridApp.sample.BuildConfig

object HybridAppConst {
    const val baseUrl = BuildConfig.BASE_URL
    const val homePage = BuildConfig.HOME_PAGE
    const val homePage2 = "${BuildConfig.HOME_PAGE}/"

    //const val homhomePageePage3 = "${BuildConfig.HOME_PAGE}/index.do"
    const val homePage3 = "${BuildConfig.HOME_PAGE}/index.do"
    const val loginPage = "/login.do"
    const val myPage = "/user/mypage/mypage.do"
    const val golfResListPage = "/user/golf/golfResList.do"
    const val condoResListPage = "/user/condo/condoResList.do"
    const val ticketPage = "/user/mypage/payMentHistory.do"
    const val buyTicketPage = "/ticket.do"
    const val myTicketPage = "/user/myTicket/myTicket.do"
    const val waitingTimePage = "/waiting.do?is_full=y"
    const val basketPage = "/user/mypage/wishList.do"
    const val presentBox = "/user/gift/giftBox.do"
    const val eventPage = "/eventView.do"    // "/user/event/eventView.do"

    const val BELLEFORET_SCHEME = "belleforetApp://"
    const val SCHEME_HOST_LOGIN = "login"           // login
    const val SCHEME_PARAM_ID = "id"                // 회원 id
    const val SCHEME_PARAM_NAME = "name"            // 회원 이름
    const val SCHEME_PARAM_MEMBERSHIP_LEVEL = "membershipLevel" // 회원 등급
    const val RES_NO = "resno" // 회원 resno
    const val SCHEME_HOST_LOGOUT = "logout"         // logout

    const val SCHEME_PARAM_BASKET = "basket"        // 장바구니 블릿 카운트
    const val SCHEME_PARAM_TITLE = "title"          // 페이지 타이틀
    const val PAGE_TITLE_LOGO = "logo"              // Page Title logo image 표시
    const val SCHEME_PARAM_MENU = "menu"            // 타이틀바 좌측 아이콘 (menu(메뉴버튼), back(백버튼))
    const val MENU_ICON_BACK = "back"               // back 버튼
    const val MENU_ICON_MENU = "ham"                // menu 버튼

    const val SCHEME_PARAM_IS_FULL = "is_full"      // full screen page 여부
    const val IS_FULL_Y = "y"                       // full screen
    const val IS_FULL_N = "n"                       // normal(네이티브 타이틀, 탭바 표시)

    // 회원 등급
    const val MEMBERSHIP_RED = "레드"         // 콘도레드회원
    const val MEMBERSHIP_GREEN = "그린"       // 콘도그린회원
    const val MEMBERSHIP_ONLINE = "온라인"     // 온라인회원
    const val MEMBERSHIP_RED_EN = "RED"
    const val MEMBERSHIP_GREEN_EN = "GREEN"
    const val MEMBERSHIP_ONLINE_EN = "ONLINE"


}