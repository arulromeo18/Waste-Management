package com.wastemanagement.wastesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper for any paginated list response across the API —
 * ManageCitizens.js, ManageWorkers.js, ComplaintsView.js, and every other
 * admin listing screen that needs page-through behavior rather than
 * returning an entire collection at once.
 *
 * Wrapping Spring Data's Page<T> directly in a controller response works,
 * but exposes several internal-only fields (pageable, sort details, etc.)
 * that the frontend never needs and that would bloat every list response.
 * This DTO extracts only what React's pagination UI actually needs
 * (content, page number, page size, total elements, total pages, and a
 * couple of convenience booleans), via the static fromPage(...) factory,
 * keeping the API contract clean and independent of Spring Data's
 * internal Page implementation details.
 *
 * The generic type parameter T lets this same wrapper serve every list
 * endpoint in the system (PagedResponse<UserResponse>,
 * PagedResponse<ComplaintResponse>, PagedResponse<ZoneResponse>, etc.)
 * without needing a bespoke paged-response class per entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;

    private int pageNumber;

    private int pageSize;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;

    /**
     * Builds a PagedResponse from a Spring Data Page<T>, extracting only
     * the fields the frontend actually needs. Used by every service
     * method that returns a paginated result (UserService, ComplaintService,
     * ZoneService, etc.) as the final step before handing the response
     * back to its controller.
     */
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}