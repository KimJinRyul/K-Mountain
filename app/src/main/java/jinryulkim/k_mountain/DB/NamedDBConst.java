package jinryulkim.k_mountain.DB;

/**
 * Created by jinryulkim on 15. 9. 9..
 */
public class NamedDBConst {
    public final static String DB_NAME = "named_mt_db";
    public final static String _ID = "_id";
    public final static String mntNm = "_mntNm";            // 산이름
    public final static String subNm = "_subNm";            // 부제
    public final static String mntnCd = "_mntnCd";          // 산코드
    public final static String areaNm = "_areaNm";          // 소재지
    public final static String mntHeight = "_mntHeight";    // 높이
    public final static String areaReason = "_areaReason";  // 선정이유
    public final static String overView = "_overView";      // 개관
    public final static String details = "_details";        // 상세
    public final static String tpTitl = "_tpTitl";          // 산행 PLUS 이름
    public final static String tpContent = "_tpContent";    // 산행 PLUS 내용
    public final static String transport = "_transport";    // 교통정보
    public final static String tourismInf = "_tourismInf";  // 주변 관광 정보
    public final static String etcCourse = "_etcCourse";    // 기타 코스
    public final static String flashUrl = "_flashUrl";      // Flash File URL
    public final static String videoUrl = "_videoUrl";      // Media File URL

    // table
    public final static String _TABLE = "_table_named_mt";
    public final static String _CREATE = "create table " + _TABLE + "(" + _ID + " integer primary key autoincrement, " +
            mntNm + " text not null, " +
            subNm + " text not null, " +
            mntnCd + " text not null, " +
            areaNm + " text not null, " +
            mntHeight + " text not null, " +
            areaReason + " text not null, " +
            overView + " text not null, " +
            details + " text not null, " +
            tpTitl + " text not null, " +
            tpContent + " text not null, " +
            transport + " text not null, " +
            tourismInf + " text not null, " +
            etcCourse + " text not null, " +
            flashUrl + " text not null, " +
            videoUrl + " text not null);";
}
