package com.notification.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class SearchResponseDto<T> {
    private boolean success;
    private String errorCode;
    private String message;
    private List<T> data;
    private PageInfo pageInfo;
    
    @Data
    public static class PageInfo {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        
        public static <T> PageInfo from(Page<T> page) {
            PageInfo info = new PageInfo();
            info.setCurrentPage(page.getNumber());
            info.setPageSize(page.getSize());
            info.setTotalElements(page.getTotalElements());
            info.setTotalPages(page.getTotalPages());
            info.setHasNext(page.hasNext());
            info.setHasPrevious(page.hasPrevious());
            return info;
        }
    }
    
    public static <T> SearchResponseDto<T> success(List<T> data, Page<?> page) {
        SearchResponseDto<T> response = new SearchResponseDto<>();
        response.setSuccess(true);
        response.setData(data);
        if (page != null) {
            response.setPageInfo(PageInfo.from(page));
        }
        return response;
    }
    
    public static <T> SearchResponseDto<T> error(String errorCode, String message) {
        SearchResponseDto<T> response = new SearchResponseDto<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }
} 