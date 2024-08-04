package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // 서버의 PORT를 랜덤으로 설정합니다.
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 테스트 인스턴스의 생성 단위를 클래스로 변경합니다.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Order Annotation을 사용할 수 있도록 설정합니다.
public class ProductServiceIntegrationTest {
    @Autowired
    ProductService productService;

    @Autowired
    UserRepository userRepository;

    User user;
    ProductResponseDto createProduct = null;
    int updateMyPrice = -1;

    @Test
    @Order(1)
    @DisplayName("신규 관심상품 등록")
    public void test1() throws Exception {
        // given
        String title = "Apple <b>에어팟</b> 2세대 유선충전 모델 (MV7N2KH/A)";
        String imageUrl = "https://shopping-phinf.pstatic.net/main_1862208/18622086330.20200831140839.jpg";
        String linkUrl = "https://search.shopping.naver.com/gate.nhn?id=18622086330";
        int lPrice = 173900;
        ProductRequestDto reqeustDto = new ProductRequestDto(title, imageUrl, linkUrl, lPrice);
        user = userRepository.findById(1L).orElse(null);

        // when
        ProductResponseDto product = productService.createProduct(reqeustDto, user);

        // then
        assertThat(product.getId()).isNotNull();
        assertThat(product.getTitle()).isEqualTo(title);
        assertThat(product.getImage()).isEqualTo(imageUrl);
        assertThat(product.getMyprice()).isEqualTo(0);
        createProduct = product;
    }

    @Test
    @Order(2)
    @DisplayName("신규 등록된 관심상품의 희망 최저가 변경")
    public void test2() throws Exception {
        // given
        Long productId = createProduct.getId();
        int myPrice = 173000;
        ProductMypriceRequestDto requestDto = new ProductMypriceRequestDto();
        requestDto.setMyprice(myPrice);

        // when
        ProductResponseDto product = productService.updateProduct(productId, requestDto);

        // then
        assertThat(product.getId()).isEqualTo(createProduct.getId());
        assertThat(product.getMyprice()).isEqualTo(myPrice);
        this.updateMyPrice = myPrice;
    }


    @Test
    @Order(3)
    @DisplayName("회원이 등록한 모든 관심상품 조회")
    public void test3() throws Exception {
        // given

        // when
        Page<ProductResponseDto> productList = productService.getProducts(user, 0, 10, "id", false);

        // then
        // 1. 전체 상품에서 테스트에 의해 생성된 상품 찾아오기 (상품의 ID로 찾음)
        Long createProductId = this.createProduct.getId();
        ProductResponseDto foundProduct = productList.
                stream()
                .filter(product -> product.getId().equals(createProductId))
                .findFirst()
                .orElse(null);

        // 2. Order(1) 테스트에 의해 생성된 상품과 일치하는지 확인
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getTitle()).isEqualTo(this.createProduct.getTitle());
        assertThat(foundProduct.getImage()).isEqualTo(this.createProduct.getImage());
    }
}
