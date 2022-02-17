package com.sxh.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author sxh
 * @date 2022/2/17
 */
@MapperScan("com.sxh.mapper")
@Configuration
public class MybatisConfig {
}
