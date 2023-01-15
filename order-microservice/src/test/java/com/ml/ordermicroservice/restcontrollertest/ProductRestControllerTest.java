package com.ml.ordermicroservice.restcontrollertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ml.ordermicroservice.dto.OrderDTO;
import com.ml.ordermicroservice.dto.PaginatedOrdersResponse;
import com.ml.ordermicroservice.dto.PaginatedProductResponse;
import com.ml.ordermicroservice.dto.ProductDTO;
import com.ml.ordermicroservice.model.Packages;
import com.ml.ordermicroservice.model.Product;
import com.ml.ordermicroservice.restcontroller.ProductRestController;
import com.ml.ordermicroservice.service.ProductService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductRestController.class)
@ActiveProfiles("test")
public class ProductRestControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should SEARCH A PRODUCT With A Product Name When Making A GET Request To Endpoint - /api/v1/product?productName=")
    public void shouldFetchProductUsingTheProductName() throws Exception {
        Product mockResponseData = sampleProductData();
        String phoneNumber = "Product1";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("productName", phoneNumber);

        Mockito.when(productService.findByStringValue(phoneNumber)).thenReturn(java.util.Optional.of(mockResponseData));

        mockMvc.perform(get("/api/v1/product").params(paramsMap)
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should FETCH Paginated Order Data Using Request Parameters(size,page,sortDirection,sortBy) Using A GET Method To The Endpoint - /api/v1/order")
    public void shouldFetchPaginatedOrders() throws Exception{
        PaginatedProductResponse mockResponseData = paginatedProductResponse();
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("page", "0");
        paramsMap.add("size", "10");
        paramsMap.add("sortBy", "id");
        paramsMap.add("sortDir", "ASC");

        Mockito.when(productService.fetchPaginatedResults(page,size,sortBy,sortDir)).thenReturn(mockResponseData);

        mockMvc.perform(get("/api/v1/product").params(paramsMap))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content[0].packages.size()").value(mockResponseData.getContent().get(0).getPackages().size()));

    }

    private PaginatedProductResponse paginatedProductResponse() {
        ProductDTO productDTO = modelMapper.map(sampleProductData(),ProductDTO.class);
        List<ProductDTO> productDTOS = Arrays.asList(productDTO,productDTO);
        return PaginatedProductResponse
                .builder()
                .content(productDTOS)
                .pageNo(0)
                .pageSize(10)
                .totalElements(3)
                .totalPages(1)
                .last(true).build();
    }

    @Test
    @DisplayName("Should  UPDATE a Customer when making a PUT Request To Endpoint -  /{id}/edit")
    public void shouldUpdateAProduct() throws Exception {
        int productId = 1;
        ProductDTO mockUpdateRequestDTO = modelMapper.map(sampleUpdateProductData(),ProductDTO.class);
        mockUpdateRequestDTO.setId(productId);
        Product mockUpdateResponseData = sampleProductData();
        mockUpdateResponseData.setId(productId);

        Mockito.when(productService.updateProduct(productId,mockUpdateRequestDTO)).thenReturn(mockUpdateResponseData);
        mockMvc.perform(MockMvcRequestBuilders
                .put("/api/v1/product/{id}/edit", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUpdateRequestDTO))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("Should Fail To UPDATE a Customer when making a PUT Request To Endpoint -  /{id}/edit")
    public void shouldFailToUpdateAProduct() throws Exception {
        int productId = 1;
        ProductDTO mockUpdateRequestDTO = modelMapper.map(sampleWrongProductData(),ProductDTO.class);
        mockUpdateRequestDTO.setId(productId);
        Product mockUpdateResponseData = sampleProductData();
        mockUpdateResponseData.setId(productId);

        Mockito.when(productService.updateProduct(productId,mockUpdateRequestDTO)).thenReturn(mockUpdateResponseData);
        mockMvc.perform(MockMvcRequestBuilders
                .put("/api/v1/product/{id}/edit", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUpdateRequestDTO))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("Should List All Products When Making A GET Request To Endpoint - /api/v1/product/")
    public void shouldListAllProducts() throws Exception{

        Product product1 = sampleProductData();
        Product product2 = sampleProductData();
        List<Product> expectedProducts = Arrays.asList(product1,product2);

        Mockito.when(productService.fetchAll()).thenReturn(expectedProducts);

        mockMvc.perform(get("/api/v1/product/"))
                .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.size()", Matchers.is(2)));
    }

    @Test
    @DisplayName("Should SAVE Products When Making A POST Request To Endpoint - /api/v1/product/")
    public void shouldSaveProduct() throws Exception{
        Product mockResponseData = sampleProductData();
       ProductDTO mockRequestDTO = modelMapper.map(sampleProductData(), ProductDTO.class);

        Mockito.when(productService.save(mockRequestDTO)).thenReturn(mockResponseData);

        mockMvc.perform(
                post("/api/v1/product")
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(mockRequestDTO))
        ).andDo(print())
                .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.data.packages.size()").value(3));
    }

    @Test
    @DisplayName("Should FAIL When SAVING Products When Making A POST Request To Endpoint - /api/v1/product/")
    public void shouldFailWhenSavingProducts() throws Exception {
        Product mockWrongProduct = sampleWrongProductData();

        mockMvc.perform(
                post("/api/v1/product")
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(mockWrongProduct))
        ).andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should DELETE A Product when making a DELETE Request To Endpoint -  /{id}")
    public void shouldDeleteACustomer() throws Exception {
        int productId = 1;

        Mockito.when(productService.deleteProduct(productId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/product/{id}", productId)
        )
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(print());
    }


    private Product sampleProductData() {
        Product product = new Product();
        List<Integer> samplePackageSize = Arrays.asList(1, 2, 3);
        List<Packages> productPackages = samplePackageSize.stream().map(w -> {
            Packages __package = new Packages();
            __package.setProductId(null);
            __package.setPackageName("Package " + w.toString());
            __package.setRate(100.21);
            __package.setCreatedAt(ZonedDateTime.now());
            __package.setUpdatedAt(ZonedDateTime.now());
            return __package;
        }).collect(Collectors.toList());
        product.setProductName("Product1");
        product.setId(null);
        product.setPackages(productPackages);
        product.setCreatedAt(ZonedDateTime.now());
        product.setUpdatedAt(ZonedDateTime.now());
        return product;
    }
    private Product sampleUpdateProductData() {
        Product product = new Product();
        List<Integer> samplePackageSize = Arrays.asList(1, 2, 3);
        List<Packages> productPackages = samplePackageSize.stream().map(w -> {
            Packages __package = new Packages();
            __package.setProductId(null);
            __package.setPackageName("Package " + w.toString());
            __package.setRate(100.21);
            __package.setCreatedAt(ZonedDateTime.now());
            __package.setUpdatedAt(ZonedDateTime.now());
            return __package;
        }).collect(Collectors.toList());
        product.setProductName("Product1");
        product.setId(null);
        product.setPackages(productPackages);
        product.setCreatedAt(ZonedDateTime.now());
        product.setUpdatedAt(ZonedDateTime.now());
        return product;
    }
    private Product sampleWrongProductData() {
        Product product = new Product();
        List<Integer> samplePackageSize = Arrays.asList(1, 2, 3);
        List<Packages> productPackages = samplePackageSize.stream().map(w -> {
            Packages __package = new Packages();
            __package.setProductId(null);
            __package.setPackageName("Package " + w.toString());
            __package.setRate(100.21);
            __package.setCreatedAt(ZonedDateTime.now());
            __package.setUpdatedAt(ZonedDateTime.now());
            return __package;
        }).collect(Collectors.toList());
        product.setProductName("");
        product.setId(null);
        product.setPackages(productPackages);
        product.setCreatedAt(ZonedDateTime.now());
        product.setUpdatedAt(ZonedDateTime.now());
        return product;
    }
}

