package guru.springframework.sfgrestdocsexample.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import static org.springframework.restdocs.snippet.Attributes.key;

import com.fasterxml.jackson.databind.ObjectMapper;

import guru.springframework.sfgrestdocsexample.domain.Beer;
import guru.springframework.sfgrestdocsexample.repositories.BeerRepository;
import guru.springframework.sfgrestdocsexample.web.model.BeerDto;
import guru.springframework.sfgrestdocsexample.web.model.BeerStyleEnum;

@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(BeerController.class)
@ComponentScan(basePackages = "guru.springframework.sfgrestdocsexample.web.mappers")
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerRepository beerRepository;

    @Test
    void getBeerById() throws Exception {

	UUID id = UUID.randomUUID();

	given(beerRepository.findById(any())).willReturn(
		Optional.of(Beer.builder().id(id).beerName("Beer Name").beerStyle("PALE_ALE").build()));

	ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

	mockMvc.perform(
		get("/api/v1/beer/{beerId}", id.toString()).param("isCold", "yes").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andDo(document("/api/v1/beer-get",
			pathParameters(
				parameterWithName("beerId").description("Unique identifier of beer to retrieve")),
			requestParameters(parameterWithName("isCold")
				.description("optional query parameter to request cold beer")),
			responseFields(fields.withPath("id").description("Unique Id of beer"),
				fields.withPath("version").description("Version"),
				fields.withPath("lastModifiedDate")
					.description("Last time this beer record was modified"),
				fields.withPath("createdDate").description("Time this beer record was created"),
				fields.withPath("beerName").description("Name of beer"),
				fields.withPath("beerStyle").description("Style of beer"),
				fields.withPath("upc").description("Universal product code of beer"),
				fields.withPath("price").description("Price of beer"),
				fields.withPath("quantityOnHand").description("Current inventory of this beer"))));
    }

    @Test
    void saveNewBeer() throws Exception {
	BeerDto beerDto = getValidBeerDto();
	String beerDtoJson = objectMapper.writeValueAsString(beerDto);

	ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

	mockMvc.perform(post("/api/v1/beer").contentType(MediaType.APPLICATION_JSON).content(beerDtoJson))
		.andExpect(status().isCreated())
		.andDo(document("/api/v1/beer-post", requestFields(
			fields.withPath("id").description("Unique Id of beer").ignored(),
			fields.withPath("version").description("Version").ignored(),
			fields.withPath("lastModifiedDate").description("Last time this beer record was modified")
				.ignored(),
			fields.withPath("createdDate").description("Time this beer record was created").ignored(),
			fields.withPath("beerName").description("Name of beer"),
			fields.withPath("beerStyle").description("Style of beer"),
			fields.withPath("upc").description("Universal product code of beer"),
			fields.withPath("price").description("Price of beer"),
			fields.withPath("quantityOnHand").description("Current inventory of this beer").ignored())));
    }

    @Test
    void updateBeerById() throws Exception {
	BeerDto beerDto = getValidBeerDto();
	String beerDtoJson = objectMapper.writeValueAsString(beerDto);

	ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

	mockMvc.perform(put("/api/v1/beer/{beerId}", UUID.randomUUID().toString())
		.contentType(MediaType.APPLICATION_JSON).content(beerDtoJson)).andExpect(status().isNoContent())
		.andDo(document("/api/v1/beer-put",
			pathParameters(parameterWithName("beerId").description("Unique identifier of beer to update")),
			requestFields(fields.withPath("id").description("Unique Id of beer").ignored(),
				fields.withPath("version").description("Version").ignored(),
				fields.withPath("lastModifiedDate")
					.description("Last time this beer record was modified").ignored(),
				fields.withPath("createdDate").description("Time this beer record was created")
					.ignored(),
				fields.withPath("beerName").description("Name of beer"),
				fields.withPath("beerStyle").description("Style of beer"),
				fields.withPath("upc").description("Universal product code of beer"),
				fields.withPath("price").description("Price of beer"), fields.withPath("quantityOnHand")
					.description("Current inventory of this beer").ignored())));
    }

    BeerDto getValidBeerDto() {
	return BeerDto.builder().beerName("Nice Ale").beerStyle(BeerStyleEnum.ALE).price(new BigDecimal("9.99"))
		.upc(123123123123L).build();

    }

    private static class ConstrainedFields {

	private final ConstraintDescriptions constraintDescriptions;

	ConstrainedFields(Class<?> input) {
	    this.constraintDescriptions = new ConstraintDescriptions(input);
	}

	private FieldDescriptor withPath(String path) {
	    return fieldWithPath(path).attributes(key("constraints").value(StringUtils
		    .collectionToDelimitedString(this.constraintDescriptions.descriptionsForProperty(path), ". ")));
	}
    }

}