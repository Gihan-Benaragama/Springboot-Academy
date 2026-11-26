package com.springbootacademy.api.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Generic Page&lt;Entity&gt; -> Page&lt;DTO&gt; converter built on ModelMapper.
 * This is where ModelMapper is used in the project (see README).
 */
@Component
public class PageMapper {

    private final ModelMapper modelMapper;

    public PageMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <S, D> Page<D> toPage(Page<S> page, Class<D> destinationType) {
        return page.map(source -> modelMapper.map(source, destinationType));
    }
}
