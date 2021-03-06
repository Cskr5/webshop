package hu.progmasters.gmistore.dto.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PagedProductList {

    private List<ProductDto> products;
    private Long totalElements;
    private Integer totalPages;
    private String categoryDisplayName;
    private Integer highestPrice;
}
