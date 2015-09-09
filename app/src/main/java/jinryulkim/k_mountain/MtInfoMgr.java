package jinryulkim.k_mountain;

import java.util.ArrayList;

/**
 * Created by jinryulkim on 15. 8. 25..
 */
public class MtInfoMgr {
    public static int deletedCnt = 0;       // User가 삭제한 개수
    public static int totalCnt = 0;         // 전체개수
    public static int pageUnit = 10;        // 페이지당 개수
    public static int pageIndex = 1;        // 현재 페이지
    public static String searchWrd = "";    // 검색어
    public static ArrayList<MtInfo_General> mMtInfos = new ArrayList<MtInfo_General>();
}
