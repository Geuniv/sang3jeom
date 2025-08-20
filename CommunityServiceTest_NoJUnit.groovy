import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import static net.grinder.util.GrinderUtils.* 
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

// nGrinder HTTP 플러그인 사용
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.common.GrinderProperties
import HTTPClient.NVPair

@RunWith(GrinderRunner)
class CommunityServiceTest {
ㄹ
    public static GTest test1
    public static GTest test2
    public static HTTPRequest request

    @BeforeProcess
    public static void beforeProcess() {
        // HTTP 요청 객체 생성
        request = new HTTPRequest()
        
        // HTTP 플러그인 전역 설정
        HTTPPluginControl.getConnectionDefaults().timeout = 10000
        HTTPPluginControl.getConnectionDefaults().followRedirects = 1
        HTTPPluginControl.getConnectionDefaults().useContentEncoding = true
        HTTPPluginControl.getConnectionDefaults().useCookies = true
        
        test1 = new GTest(1, "게시글 목록 조회")
        test2 = new GTest(2, "게시글 좋아요")
        
        grinder.logger.info("Starting Community Service Performance Test - ALB Direct Access")
    }

    @BeforeThread 
    public void beforeThread() {
        // 기본 헤더 설정
        request.setHeaders([
            new NVPair("User-Agent", "nGrinder-LoadTest"),
            new NVPair("Accept", "application/json"),
            new NVPair("Content-Type", "application/json")
        ] as NVPair[])
        
        grinder.statistics.delayReports = true
        test1.record(this, "testGetGoodsPosts")
        test2.record(this, "testLikePost")
    }

    @Test
    public void testGetGoodsPosts() {
        // ALB 직접 HTTP 접근 (SSL 우회)
        def result = request.GET("http://a0fa9acba723e43c6b9bd0a34daa589e-3b703a91ce113926.elb.ap-northeast-2.amazonaws.com/goods-posts")
        
        if (result.statusCode == 200) {
            grinder.logger.info("게시글 목록 조회 성공: " + result.statusCode)
        } else {
            grinder.logger.error("게시글 목록 조회 실패: " + result.statusCode)
        }
        
        assertThat(result.statusCode, lessThan(400))
    }

    // @Test  
    // public void testLikePost() {
    //     // 임의의 게시글 ID (1~4 사이)
    //     int postId = (grinder.getRunNumber() % 4) + 1
        
    //     try {
    //         // ALB 직접 HTTP 접근으로 좋아요 테스트
    //         def result = request.POST("http://a0fa9acba723e43c6b9bd0a34daa589e-3b703a91ce113926.elb.ap-northeast-2.amazonaws.com/likes/" + postId)
            
    //         if (result.statusCode == 200) {
    //             grinder.logger.info("좋아요 성공 - 게시글 " + postId + ": " + result.statusCode)
    //         } else if (result.statusCode == 401) {
    //             grinder.logger.info("인증 오류 (예상됨) - 게시글 " + postId + ": " + result.statusCode)
    //         } else if (result.statusCode == 403) {
    //             grinder.logger.info("권한 오류 (예상됨) - 게시글 " + postId + ": " + result.statusCode)
    //         } else {
    //             grinder.logger.warn("예상치 못한 응답 - 게시글 " + postId + ": " + result.statusCode)
    //         }
            
    //         // 성공 조건 (200, 401, 403 모두 정상적인 응답으로 간주)
    //         assertThat(result.statusCode, anyOf(is(200), is(401), is(403)))
            
    //     } catch (Exception e) {
    //         grinder.logger.error("좋아요 테스트 예외 발생: " + e.getMessage())
    //     }
    // }
}