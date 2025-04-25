package com.flexrate.flexrate_back.common.dto;

public record PaginationInfo(
        int currentPage,
        int pageSize,
        int totalPages,
        long totalElements
) {}
