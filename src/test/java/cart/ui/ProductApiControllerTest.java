package cart.ui;

import static cart.RestDocsHelper.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import cart.RestDocsHelper;
import cart.application.ProductService;
import cart.dao.MemberDao;
import cart.domain.Product;
import cart.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductApiController.class)
@AutoConfigureRestDocs
class ProductApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProductService productService;

    @MockBean
    MemberDao memberDao;

    @Test
    @DisplayName("/products로 GET 요청")
    void findAll_products() throws Exception {
        // given
        given(productService.getAllProducts())
                .willReturn(List.of(
                        ProductResponse.of(
                                new Product(1L, "치킨", 20000, "http://image.com/image.jpg"))
                ));
        // when

        // then
        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(customDocument("findAll",
                        responseFields(
                            fieldWithPath("[0].id").description("상품 ID"),
                            fieldWithPath("[0].name").description("상품 이름"),
                            fieldWithPath("[0].price").description("상품 가격"),
                            fieldWithPath("[0].imageUrl").description("상품 이미지 URL")
                        )));
    }

}
