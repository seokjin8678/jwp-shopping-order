package cart.ui;

import static cart.helper.RestDocsHelper.prettyDocument;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cart.MockAuthProviderConfig;
import cart.application.OrderService;
import cart.config.AuthProvider;
import cart.dto.User;
import cart.dao.MemberDao;
import cart.domain.Member;
import cart.dto.request.OrderItemRequest;
import cart.dto.request.OrderRequest;
import cart.dto.response.OrderDetailResponse;
import cart.dto.response.OrderItemResponse;
import cart.dto.response.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureRestDocs
@Import(MockAuthProviderConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderControllerTest {
    private final Member member = new Member(1L, "aa@aaa.com", "1234", 1000);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderService orderService;

    @MockBean
    MemberDao memberDao;

    @Autowired
    AuthProvider authProvider;

    @BeforeEach
    void setUp() {
        given(authProvider.resolveUser(anyString()))
                .willReturn(new User(1L, "a@a.com"));
    }

    @Test
    void 주문_요청을_정상적으로_처리한다() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(1L, 10);
        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest), 1000L);
        given(orderService.createOrder(any(OrderRequest.class), anyLong()))
                .willReturn(1L);
        given(memberDao.findByEmail(any())).willReturn(Optional.of(member));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(prettyDocument(
                        "orders/create",
                        requestHeaders(
                                headerWithName("Authorization").description("회원 인증 Basic 토큰")
                        ),
                        requestFields(
                                fieldWithPath("orderItems").description("주문하는 상품 및 수량"),
                                fieldWithPath("orderItems[].productId").description("주문하는 상품의 ID"),
                                fieldWithPath("orderItems[].quantity").description("주문하는 상품의 수량"),
                                fieldWithPath("spendPoint").description("결제 시 사용한 포인트")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("message").description("주문이 정상적으로 처리되었습니다.")
                        )
                ));
    }

    @Test
    void 주문을_요청할때_품목의_ID가_포함되지_않으면_400_상태코드가_반환된다() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(null, 10);
        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest), 1000L);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result['orderItems[0].productId']").value("상품 ID는 반드시 포함되어야 합니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void 주문을_요청할때_품목의_ID가_0_또는_음수이면_400_상태코드가_반환된다(Long productId) throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(productId, 10);
        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest), 1000L);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result['orderItems[0].productId']").value("상품 ID는 0 또는 음수가 될 수 없습니다."));
    }


    @Test
    void 주문을_요청할때_품목의_수량이_포함되지_않으면_400_상태코드가_반환된다() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(1L, null);
        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest), 1000L);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result['orderItems[0].quantity']").value("수량은 반드시 포함되어야 합니다."));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void 주문을_요청할때_품목의_수량이_0_또는_음수이면_400_상태코드가_반환된다(Integer quantity) throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(1L, quantity);
        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest), 1000L);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result['orderItems[0].quantity']").value("수량은 0 또는 음수가 될 수 없습니다."));
    }

    @Test
    void 주문을_요청할때_사용할_포인트가_포함되지_않으면_400_상태코드가_반환된다() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(1L, 3);
        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest), null);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.spendPoint").value("사용할 포인트는 반드시 포함되어야 합니다."));
    }

    @Test
    void 주문을_요청할때_사용할_포인트가_음수이면_400_상태코드가_반환된다() throws Exception {
        OrderItemRequest orderItemRequest = new OrderItemRequest(1L, 3);
        OrderRequest orderRequest = new OrderRequest(List.of(orderItemRequest), -1L);

        mockMvc.perform(post("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.spendPoint").value("사용할 포인트는 음수가 될 수 없습니다."));
    }

    @Test
    void 회원의_모든_주문을_조회_요청을_정상적으로_처리한다() throws Exception {
        OrderResponse orderResponse = new OrderResponse(1L, "http://image.com/image.png","감자",2, 10000L, LocalDateTime.now());
        given(orderService.findAllOrders(anyLong()))
                .willReturn(List.of(orderResponse));
        given(memberDao.findByEmail(any())).willReturn(Optional.of(member));

        mockMvc.perform(get("/orders")
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(prettyDocument("orders/findAll",
                        requestHeaders(
                                headerWithName("Authorization").description("회원 인증 Basic 토큰")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("result[].orderId").description("주문 ID"),
                                fieldWithPath("result[].thumbnail").description("주문 상품 대표 썸네일 URL"),
                                fieldWithPath("result[].firstProductName").description("대표 상품 이름"),
                                fieldWithPath("result[].totalCount").description("주문 상품 수량"),
                                fieldWithPath("result[].spendPrice").description("주문 총 금액"),
                                fieldWithPath("result[].createdAt").description("주문 생성 시간")
                        )
                ));
    }

    @Test
    void 회원의_특정_주문_조회_요청을_정상적으로_처리한다() throws Exception {
        OrderItemResponse orderItemResponse = new OrderItemResponse(1L, "사과", "http:image.com", 1000L, 10);
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse(1L, 1000L, 500L, 500L, LocalDateTime.now(),
                List.of(orderItemResponse));
        given(orderService.findOrderById(anyLong(), anyLong()))
                .willReturn(orderDetailResponse);
        given(memberDao.findByEmail(any())).willReturn(Optional.of(member));

        mockMvc.perform(get("/orders/{orderId}", 1L)
                        .header("Authorization", "basic " + "YUBhLmNvbToxMjM0"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(prettyDocument(
                        "orders/findOne",
                        requestHeaders(
                                headerWithName("Authorization").description("회원 인증 Basic 토큰")
                        ),
                        pathParameters(
                                parameterWithName("orderId").description("주문 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("result.orderId").description("주문 ID"),
                                fieldWithPath("result.totalPrice").description("주문 전체 가격"),
                                fieldWithPath("result.spendPoint").description("주문에 사용한 포인트"),
                                fieldWithPath("result.spendPrice").description("실제 결제 금액"),
                                fieldWithPath("result.createdAt").description("주문 생성 시간"),
                                fieldWithPath("result.orderItemResponses[].productId").description("주문 상품 ID"),
                                fieldWithPath("result.orderItemResponses[].name").description("주문 상품 이름"),
                                fieldWithPath("result.orderItemResponses[].imageUrl").description("주문 상품 이미지 URL"),
                                fieldWithPath("result.orderItemResponses[].price").description("주문 상품 가격"),
                                fieldWithPath("result.orderItemResponses[].quantity").description("주문 상품 수량")
                        )
                ));
    }
}
