# 				项目Java代码解析

## 一、com.tps.springbot-common

#### 1.com.tps.springbot-common-Constants

```
package com.tps.springboot.common;

public interface Constants {

    String CODE_200 = "200"; //成功
    String CODE_401 = "401";  // 权限不足
    String CODE_400 = "400";  // 参数错误
    String CODE_500 = "500"; // 系统错误
    String CODE_600 = "600"; // 其他业务异常

    String DICT_TYPE_ICON = "icon";

    String FILES_KEY = "FILES_FRONT_ALL";

}
```

​	这段 Java 代码定义了一个名为 `Constants` 的接口，接口的作用是存储项目中使用的常量，方便在整个项目里复用和维护这些常量。此接口定义了一系列常量，包含状态码、字典类型和文件键，这些常量在整个项目中可被多个类引用，增强了代码的可读性和可维护性。在项目里，当需要使用这些常量时，可直接通过接口名来引用。



#### 2.com.tps.springbot-common-Result

```
package com.tps.springboot.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口统一返回包装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private String code;
    private String msg;
    private Object data;

    public static Result success() {
        return new Result(Constants.CODE_200, "", null);
    }

    public static Result success(Object data) {
        return new Result(Constants.CODE_200, "", data);
    }

    public static Result error(String code, String msg) {
        return new Result(code, msg, null);
    }

    public static Result error() {
        return new Result(Constants.CODE_500, "系统错误", null);
    }

}
```

​	这段 Java 代码定义了一个名为 `Result` 的类，它是一个接口统一返回包装类，用于在项目中统一处理接口的返回结果。`Result` 类的主要作用是统一接口的返回格式，通过静态方法可以方便地创建表示成功或失败的返回对象，提高代码的可读性和可维护性。在项目中，当需要返回接口结果时，可以直接调用这些静态方法来创建 `Result` 对象，然后将其作为接口的返回值。



#### 3.com.tps.springbot-common-RoleEnum

```
package com.tps.springboot.common;

public enum RoleEnum {
    ROLE_ADMIN, ROLE_MAINTENANCE, ROLE_WORKER;
}
```

​	这段 Java 代码定义了一个名为 `RoleEnum` 的枚举类型，其作用是列举出系统里不同的角色。`RoleEnum` 枚举类型定义了三种不同的角色，在项目中可用于权限管理、角色验证等功能。通过使用枚举类型，代码的可读性和可维护性得到了提高，同时也避免了使用字符串或整数来表示角色时可能出现的错误。



## 二、com.tps.springbot-config

#### 4.com.tps.springbot-config-interceptor-Jwtlnterceptor

```
package com.tps.springboot.config.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.tps.springboot.entity.User;
import com.tps.springboot.exception.ServiceException;
import com.tps.springboot.service.IUserService;
import com.tps.springboot.common.Constants;
import com.tps.springboot.config.AuthAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");

        // 如果不是映射到方法直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        } else {
            HandlerMethod h = (HandlerMethod) handler;
            AuthAccess authAccess = h.getMethodAnnotation(AuthAccess.class);
            if (authAccess != null) {
                return true;
            }
        }
        // 执行认证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException(Constants.CODE_401, "无token，请重新登录");
        }
        // 获取 token 中的 user id
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (JWTDecodeException j) {
            throw new ServiceException(Constants.CODE_401, "token验证失败，请重新登录");
        }
        // 根据token中的userid查询数据库
        User user = userService.getById(userId);
        if (user == null) {
            throw new ServiceException(Constants.CODE_401, "用户不存在，请重新登录");
        }
        // 用户密码加签验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token); // 验证token
        } catch (JWTVerificationException e) {
            throw new ServiceException(Constants.CODE_401, "token验证失败，请重新登录");
        }
        return true;
    }
}
```

​	这段 Java 代码定义了一个名为 `JwtInterceptor` 的拦截器类，它实现了 `HandlerInterceptor` 接口，用于在 Spring Boot 应用中对 JWT（JSON Web Token）进行验证。综上所述，`JwtInterceptor` 拦截器的主要功能是在请求处理之前对 JWT 进行验证，确保请求的合法性。如果验证失败，会抛出相应的异常，提示用户重新登录。



#### 5.com.tps.springbot-config-AsyncConfiguration

```
package com.tps.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean("async")
    public Executor doSomethingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：线程池创建时候初始化的线程数
        executor.setCorePoolSize(10);
        // 最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(20);
        // 缓冲队列：用来缓冲执行任务的队列
        executor.setQueueCapacity(500);
        // 允许线程的空闲时间60秒：当超过了核心线程之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("async-");
        // 缓冲队列满了之后的拒绝策略：由调用线程处理（一般是主线程）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
```

​	这段 Java 代码定义了一个 Spring 配置类 `AsyncConfiguration`，其主要功能是配置一个异步执行的线程池。`AsyncConfiguration` 类配置了一个异步执行的线程池，通过设置核心线程数、最大线程数、缓冲队列容量、线程空闲时间、线程名称前缀和拒绝策略等参数，确保线程池能够高效地处理异步任务。在 Spring 应用中，可以使用 `@Async` 注解标注方法，并通过 `@Qualifier("async")` 引用这个线程池，让方法在这个线程池中异步执行。



#### 6.com.tps.springbot-config-AuthAccess

```
package com.tps.springboot.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthAccess {

}
```

​	这段 Java 代码定义了一个名为 `AuthAccess` 的自定义注解。综上所述，`AuthAccess` 注解是一个用于标记方法的注解，主要用于标识那些不需要进行身份验证的方法，以此来实现灵活的权限控制。



#### 7.com.tps.springbot-config-CorsConfig

```
package com.tps.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    // 当前跨域请求最大有效时长。这里默认1天
    private static final long MAX_AGE = 24 * 60 * 60;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1 设置访问源地址
        corsConfiguration.addAllowedHeader("*"); // 2 设置访问源请求头
        corsConfiguration.addAllowedMethod("*"); // 3 设置访问源请求方法
        corsConfiguration.setMaxAge(MAX_AGE);
        source.registerCorsConfiguration("/**", corsConfiguration); // 4 对接口配置跨域设置
        return new CorsFilter(source);
    }
}
```

​	这段 Java 代码定义了一个 Spring 配置类 `CorsConfig`，其主要功能是配置跨域资源共享（CORS）。CORS 是一种机制，它允许浏览器在跨域请求时，服务器能够控制哪些来源的请求可以访问其资源。`CorsConfig` 类的作用是配置 Spring Boot 应用的 CORS 规则，允许所有来源的请求访问服务器的所有接口，使用所有的请求头和请求方法，并且设置了跨域请求的最大有效时长为 1 天。这样可以解决前端页面在不同域名下访问后端接口时的跨域问题。需要注意的是，在生产环境中，为了安全起见，通常不建议使用 `*` 通配符，而是应该明确指定允许的来源、请求头和请求方法。



#### 8.com.tps.springbot-config-InterceptorConfig

```
package com.tps.springboot.config;

import com.tps.springboot.config.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**")  // 拦截所有请求，通过判断token是否合法来决定是否需要登录
                .excludePathPatterns("/user/login", "/user/register", "/**/export", "/**/import", "/file/**","/python/**","/DataTest/**","/detilebord/**","/message/**",
                        "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**", "/api", "/api-docs", "/api-docs/**")
                .excludePathPatterns( "/**/*.html", "/**/*.js", "/**/*.css", "/**/*.woff", "/**/*.ttf");


    }

    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

}
```

​	这段 Java 代码定义了一个 Spring 配置类 `InterceptorConfig`，其主要功能是配置拦截器，特别是 `JwtInterceptor` 拦截器的拦截规则。`InterceptorConfig` 类的主要作用是配置 `JwtInterceptor` 拦截器的拦截规则。通过 `addPathPatterns` 方法设置拦截所有请求，然后通过 `excludePathPatterns` 方法排除一些不需要进行 JWT 验证的接口和静态资源文件。这样可以确保只有需要进行身份验证的请求才会经过 `JwtInterceptor` 拦截器进行处理，提高系统的安全性和性能。



#### 9.com.tps.springbot-config-MybatisPlusConfig

```
package com.tps.springboot.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.tps.springboot.mapper")
public class MybatisPlusConfig {

    // 最新版
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}
```

​	这段 Java 代码定义了一个 Spring 配置类 `MybatisPlusConfig`，其主要功能是对 MyBatis-Plus 进行配置，特别是启用分页插件。`MybatisPlusConfig` 类的主要作用是配置 MyBatis-Plus 的分页插件。通过创建 `MybatisPlusInterceptor` 实例并添加 `PaginationInnerInterceptor` 内部拦截器，实现了对数据库查询的分页功能。同时，使用 `@MapperScan` 注解指定了 Mapper 接口所在的包路径，方便 Spring 自动扫描和注册 Mapper 接口。在项目中，当使用 MyBatis-Plus 进行数据库操作时，就可以直接使用分页功能，



#### 10.com.tps.springbot-config-SwaggerConfig

```
package com.tps.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableOpenApi
public class SwaggerConfig {


    /**
     * 创建API应用
     * apiInfo() 增加API相关信息
     * 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
     * 本例采用指定扫描的包路径来定义指定要建立API的目录。
     *
     * @return
     */
    @Bean
    public Docket restApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("标准接口")
                .apiInfo(apiInfo("Spring Boot中使用Swagger2构建RESTful APIs", "1.0"))
                .useDefaultResponseMessages(true)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tps.springboot.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 创建该API的基本信息（这些基本信息会展现在文档页面中）
     * 访问地址：http://ip:port/swagger-ui.html
     *
     * @return
     */
    private ApiInfo apiInfo(String title, String version) {
        return new ApiInfoBuilder()
                .title(title)
                .version(version)
                .build();
    }


}
```

这段 Java 代码定义了一个 Spring 配置类 `SwaggerConfig`，其主要功能是配置 Swagger 来自动生成 RESTful API 的文档。`SwaggerConfig` 类的主要作用是配置 Swagger 来自动生成 RESTful API 的文档。通过配置 `Docket` 实例，指定了 API 的分组、基本信息、扫描的包路径等，使得 Swagger 能够扫描到 `com.tps.springboot.controller` 包下的控制器类中的 API，并将其展示在 Swagger 文档中。你可以通过访问 `http://ip:port/swagger-ui.html` 来查看生成的 API 文档，其中 `ip` 是服务器的 IP 地址，`port` 是应用程序的端口号。这样可以方便开发人员和测试人员查看和测试 API。



## 三、com.tps.springbot-controller

#### 11.com.tps.springbot-controller-dto-UserDto

```
package com.tps.springboot.controller.dto;

import com.tps.springboot.entity.Menu;
import lombok.Data;

import java.util.List;

/**
 * 接受前端登录请求的参数
 */
@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String password;
    private String nickname;
    private String avatarUrl;
    private String token;
    private String role;
    private Integer roleid;
    private List<Menu> menus;
}
```

​	这段 Java 代码定义了一个名为 `UserDTO` 的数据传输对象（Data Transfer Object，简称 DTO）类，它位于 `com.tps.springboot.controller.dto` 包下，主要用于在前端和后端之间传输用户相关的数据。

​	`UserDTO` 类主要用于在前端登录请求和后端处理之间传输用户相关的数据。通过使用 DTO 类，可以将前端传递的数据封装成一个对象，方便在后端进行处理和验证。同时，使用 Lombok 的 `@Data` 注解简化了代码的编写，提高了开发效率。在实际应用中，当用户登录成功后，后端可以将包含用户信息、角色信息和菜单信息的 `UserDTO` 对象返回给前端，前端根据这些信息进行页面展示和权限控制。



#### 12.com.tps.springbot-controller-dto-UserPasswordDTO

```
package com.tps.springboot.controller.dto;

import lombok.Data;

@Data
public class UserPasswordDTO {
    private String username;
    private String password;
    private String newPassword;
}
```

​	这段 Java 代码定义了一个名为 `UserPasswordDTO` 的数据传输对象（Data Transfer Object，简称 DTO）类，它位于 `com.tps.springboot.controller.dto` 包下，主要用于在前端和后端之间传输与用户密码修改相关的数据。

​	`UserPasswordDTO` 类的主要用途是在用户修改密码的业务场景中，封装前端传递过来的与密码修改相关的数据。前端将用户名、当前密码和新密码封装在这个 DTO 对象中发送给后端，后端接收该对象后，就能方便地获取所需信息进行密码验证和修改操作。由于使用了 Lombok 的 `@Data` 注解，代码简洁且易于维护。



#### 13.com.tps.springbot-controller-DetilebordController

```
package com.tps.springboot.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.OnlineDate;
import com.tps.springboot.mapper.ResultMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/detilebord")
public class DetilebordController {
    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Value("${server.ip}")
    private String serverIp;

    @Resource
    private ResultMapper resultMapper;
    //private  FileMapper fileMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/detail/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(resultMapper.selectById(id));
    }

    //清除一条缓存，key为要清空的数据
//    @CacheEvict(value="files",key="'frontAll'")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        resultMapper.deleteById(id);
        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

    @GetMapping("/totle")
    public Result totle() {
        List<OnlineDate> onlinedates = resultMapper.selectList(new QueryWrapper<OnlineDate>());
        String today = DateUtil.today();
        Integer totle=0;
        for (OnlineDate onlineDate : onlinedates) {
            Date createTime = onlineDate.getCreateTime();
            String format = DateUtil.format(createTime, "yyyy-MM-dd");
            if (format.equals(today)){
                totle++;
            }

        }
        return Result.success(totle);
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // select * from sys_file where id in (id,id,id...)
        QueryWrapper<OnlineDate> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<OnlineDate> onlinedates = resultMapper.selectList(queryWrapper);
        for (OnlineDate onlineDate : onlinedates) {
            resultMapper.deleteById(onlineDate);
        }
        return Result.success();
    }

    /**
     * 分页查询接口
     * @param pageNum
     * @param pageSize
     * @param result
     * @return
     */

    @GetMapping("/page/{id}")
    public Result findPage(@PathVariable Integer id,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") Integer result) {
        //LambdaQueryWrapper<OnlineDate> queryWrapper = new LambdaQueryWrapper<>();
        QueryWrapper<OnlineDate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("testfile_id",id);
//        queryWrapper.eq(OnlineDate::getTestfileid,id);
        //queryWrapper.eq("is_delete", false);
        //queryWrapper.orderByDesc("id");
        //queryWrapper.like("label", label);
       if (result!=null) {
           queryWrapper.like("result", result);
        }
        return Result.success(resultMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper));
    }

    // 设置缓存
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 删除缓存
    private void flushRedis(String key) {
        stringRedisTemplate.delete(key);
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") Integer result) {
        //LambdaQueryWrapper<OnlineDate> queryWrapper = new LambdaQueryWrapper<>();
        QueryWrapper<OnlineDate> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("testfile_id",id);
//        queryWrapper.eq(OnlineDate::getTestfileid,id);
        //queryWrapper.eq("is_delete", false);
        //queryWrapper.orderByDesc("id");
        //queryWrapper.like("label", label);
        if (result!=null) {
            queryWrapper.like("result", result);
        }
        return Result.success(resultMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper));
    }
}
```

​	这段 Java 代码定义了一个名为`DetilebordController`的控制器类，用于处理与系统某个模块（从代码推测可能与某种测试结果详情展示相关）相关的 HTTP 请求。综上所述，`DetilebordController`类提供了对`OnlineDate`数据的查询、删除等操作接口，并结合 Redis 缓存进行数据管理，以满足前端页面对于数据展示和交互的需求。



#### 14.com.tps.springbot-controller-EchartsController

```
package com.tps.springboot.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.OnlineDate;
import com.tps.springboot.mapper.ResultMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/echarts")
public class EchartsController {


    @Resource
    private ResultMapper resultMapper;


    @GetMapping("/members")
    public Result members() {
        ArrayList<Long> integers = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LambdaQueryWrapper<OnlineDate> dateLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dateLambdaQueryWrapper.eq(OnlineDate::getResult, i);
            Long aLong = resultMapper.selectCount(dateLambdaQueryWrapper);
            integers.add(aLong);
        }
        return Result.success(integers);
    }
    @GetMapping("/index")
    public Result index() {
        Map<Integer,List> map = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            LambdaQueryWrapper<OnlineDate> dateLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dateLambdaQueryWrapper.eq(OnlineDate::getResult, i);
            List<OnlineDate> dates = resultMapper.selectList(dateLambdaQueryWrapper);
            map.put(i,dates);
        }
        return Result.success(map);
    }
    @GetMapping("/totle")
    public Result totle() {
        LambdaQueryWrapper<OnlineDate> dateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long aLong = resultMapper.selectCount(dateLambdaQueryWrapper);
        return Result.success(aLong);
    }

    @GetMapping("/totle1")
    public Result totle1() {
        List<OnlineDate> onlineDates = resultMapper.selectList(new QueryWrapper<OnlineDate>());
        String today = DateUtil.today();
        Integer totle=0;
        for (OnlineDate onlineDate : onlineDates) {
            Date createTime = onlineDate.getCreateTime();
            String format = DateUtil.format(createTime, "yyyy-MM-dd");
            if (format.equals(today)){
                totle++;
            }
        }
        return Result.success(totle);
    }
    @GetMapping("/totle3")
    public Result totle3() {
        Long max=0l;
        Map<Integer,Long> map = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            LambdaQueryWrapper<OnlineDate> dateLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dateLambdaQueryWrapper.eq(OnlineDate::getResult, i);
            Long aLong = resultMapper.selectCount(dateLambdaQueryWrapper);
            map.put(i,aLong);
        }
        Integer maxKey = null;
        for (Integer key : map.keySet()) {
            if (maxKey == null || map.get(key) > map.get(maxKey)) {
                maxKey = key;
            }
        }
        System.out.println(maxKey);
        return Result.success(maxKey);
    }
//    @GetMapping("/Max")
//    public Result Max() {
//        List<Long> integers = new ArrayList<>();
//        for (int i = 0; i < 6; i++) {
//            LambdaQueryWrapper<OnlineDate> dateLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            dateLambdaQueryWrapper.eq(OnlineDate::getResult, i);
//            Long aLong = resultMapper.selectCount(dateLambdaQueryWrapper);
//            integers.add(aLong);
//        }
//        Integer max = Collections.max(integers).intValue();
//
//        return Result.success(max+300);
//    }
}
```

​	这段 Java 代码定义了一个名为`EchartsController`的控制器类，主要用于为 Echarts 图表提供数据支持。`EchartsController`类提供了多个接口，用于查询和统计`OnlineDate`表中的数据，并以适合 Echarts 图表展示的格式返回数据，满足前端图表展示的需求。



#### 15.com.tps.springbot-controller-FileController

```
package com.tps.springboot.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Files;
import com.tps.springboot.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文件上传相关接口
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Value("${server.ip}")
    private String serverIp;

    @Resource
    private FileMapper fileMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 文件上传接口
     * @param file 前端传递过来的文件
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        File uploadFile = new File(fileUploadPath + originalFilename);
        File parentFile = uploadFile.getParentFile();
        if(!parentFile.exists()) {
            parentFile.mkdirs();
        }
        String url;
        file.transferTo(uploadFile);
        url = "http://" + serverIp + ":9090/file/" + originalFilename;
        flushRedis(Constants.FILES_KEY);
        return url;
    }
    /**
     * 文件下载接口   http://localhost:9090/file/{fileUUID}
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        // 根据文件的唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);;
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }
    /**
     * 通过文件的md5查询文件
     * @param md5
     * @return
     */
    private Files getFileByMd5(String md5) {
        // 查询文件的md5是否存在
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5", md5);
        List<Files> filesList = fileMapper.selectList(queryWrapper);
        return filesList.size() == 0 ? null : filesList.get(0);
    }

//    @CachePut(value = "files", key = "'frontAll'")
    @PostMapping("/update")
    public Result update(@RequestBody Files files) {
        System.out.println("cccc"+files.getName());
        fileMapper.updateById(files);
        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(fileMapper.selectById(id));
    }

    //清除一条缓存，key为要清空的数据
//    @CacheEvict(value="files",key="'frontAll'")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        fileMapper.deleteById(id);
        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // select * from sys_file where id in (id,id,id...)
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<Files> files = fileMapper.selectList(queryWrapper);
        for (Files file : files) {
            fileMapper.deleteById(file);
        }
        return Result.success();
    }
    /**
     * 分页查询接口
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        // 查询未删除的记录
        queryWrapper.eq("is_delete", false);
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }
        return Result.success(fileMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper));
    }

    // 设置缓存
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }
    // 删除缓存
    private void flushRedis(String key) {
        stringRedisTemplate.delete(key);
    }

}
```

​	这段 Java 代码定义了一个名为`FileController`的控制器类，主要用于处理与文件上传、下载、查询和删除等相关的 HTTP 请求。

​	`FileController`类提供了一套完整的文件管理接口，包括文件的上传、下载、查询、更新和删除等操作，并结合 Redis 缓存来提高数据的访问性能和一致性。



#### 16.com.tps.springbot-controller-IndexController

```
package com.tps.springboot.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.TestFiles;
import com.tps.springboot.mapper.TestFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/home")
public class IndexController {

    @Autowired
    private TestFileMapper testFileMapper;
    @GetMapping("/members")
    public Result members(){
        int w1=0;
        int w2=0;
        int w3=0;
        int w4=0;
        int w5=0;
        int w6=0;
        int w7=0;
        LambdaQueryWrapper<TestFiles> filesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<TestFiles> testFiles = testFileMapper.selectList(filesLambdaQueryWrapper);
        for (TestFiles testFile : testFiles) {
            Date createTime = testFile.getCreateTime();
            Week week = DateUtil.dayOfWeekEnum(createTime);
            switch (week){
                case SUNDAY:w7+=1;break;
                case MONDAY:w1+=1;break;
                case TUESDAY:w2+=1;break;
                case WEDNESDAY:w3+=1;break;
                case THURSDAY:w4+=1;break;
                case FRIDAY:w5+=1;break;
                case SATURDAY:w6+=1;break;
                default:break;
            }
        }
        return Result.success(CollUtil.newArrayList(w1,w2,w3,w4,w5,w6,w7));
    }
}
```

​	这段 Java 代码定义了一个名为`IndexController`的控制器类，主要用于处理与系统首页相关的 HTTP 请求，具体功能是统计一周内每天创建的`TestFiles`记录数量。

`		IndexController`类的`members`方法实现了统计一周内每天创建的`TestFiles`记录数量的功能，并以列表形式返回统计结果，可能用于前端展示一周内数据创建的分布情况。



#### 17.com.tps.springbot-controller-MenuController

```
package com.tps.springboot.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Dict;
import com.tps.springboot.entity.Menu;
import com.tps.springboot.mapper.DictMapper;
import com.tps.springboot.service.IMenuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/menu")
@Transactional
public class MenuController {

    @Resource
    private IMenuService menuService;

    @Resource
    private DictMapper dictMapper;

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Menu menu) {
        menuService.saveOrUpdate(menu);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        menuService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        menuService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/ids")
    public Result findAllIds() {
        return Result.success(menuService.list().stream().map(Menu::getId));
    }

    @GetMapping
    public Result findAll(@RequestParam(defaultValue = "") String name) {
        return Result.success(menuService.findMenus(name));
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(menuService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        queryWrapper.orderByDesc("id");
        return Result.success(menuService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    @GetMapping("/icons")
    public Result getIcons() {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", Constants.DICT_TYPE_ICON);
        return Result.success(dictMapper.selectList(queryWrapper));
    }

}
```

​	这段 Java 代码定义了一个名为`MenuController`的控制器类，主要用于处理与菜单（`Menu`）相关的 HTTP 请求，同时也涉及部分字典（`Dict`）数据的查询。`MenuController`类提供了一套完整的菜单管理接口，包括菜单的增删改查操作，同时还提供了获取图标字典数据的接口，方便前端展示和使用相关数据。



#### 18.com.tps.springbot-controller-MessageController

```
package com.tps.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Message;
import com.tps.springboot.mapper.MessageMapper;
import com.tps.springboot.mapper.UserMapper;
import com.tps.springboot.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private MessageService messageService;
    @Resource
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/send")
    public Result save(@RequestBody Message message) {
        messageService.saveMessage(message);
        return Result.success();
    }
    @PostMapping("/sendToUpdate")
    public Result update(@RequestBody Message message) {
        messageService.updateMessage(message);
        return Result.success();
    }

    /**
     * 分页查询接口
     * @return
     */
    @GetMapping("/getUserName")
    public Result getUserName() {
        return Result.success(userMapper.selectList(new QueryWrapper<>()));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String sendUserName) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("is_delete", false);
       // queryWrapper.orderByDesc("id");
        if (!"".equals(sendUserName)) {
            queryWrapper.like("send_user_name", sendUserName);
        }
        return Result.success(messageMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper));
    }


    @GetMapping("/findById/{id}")
    public Result findById(@PathVariable Integer id) {
        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        messageLambdaQueryWrapper.eq(Message::getId,id);
        Message message = messageMapper.selectOne(messageLambdaQueryWrapper);
        return Result.success(message);
    }
    // 设置缓存
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 删除缓存
    private void flushRedis(String key) {
        stringRedisTemplate.delete(key);
    }


}
```

​	这段 Java 代码定义了一个名为`MessageController`的控制器类，它是 Spring Boot 应用中用于处理与消息（`Message`）相关的 HTTP 请求的部分。

`	MessageController`类提供了一系列与消息相关的 RESTful 接口，包括消息的发送、更新、分页查询和按 ID 查询，同时还提供了获取用户信息的接口，并且具备 Redis 缓存操作的能力，但缓存操作目前未在现有接口中使用。



#### 19.com.tps.springbot-controller-PredictController

```
package com.tps.springboot.controller;

import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.service.IPredictService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 文件上传相关接口
 */
@RestController
@RequestMapping("/DataTest")
@Transactional
public class PredictController {

    @Resource
    private IPredictService predictService;


    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file) throws Exception {

        if(null == file)
        {
            return Result.error(Constants.CODE_400,"上传的文件为空,请上传文件！");
        }

        predictService.uploadPredictFile(file);

        return Result.success();
    }

   // @Async
    @GetMapping("/getUrl/{url}")
    public Result beginPredict(@PathVariable String url) throws IOException {
        System.out.println("开始多线程,在线预测 + url");
        // 根据文件的唯一标识码获取文件
        long stime = System.currentTimeMillis();

        Result response = predictService.beginPredict(url);

        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("执行时长：%d 毫秒.", (etime - stime));
        System.out.println("结束多线程,在线预测结束");
        return response;
    }

    /**
     * 分页查询预测列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {

        return Result.success(predictService.findPage(pageNum,pageSize,name));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        predictService.deletePredictData(id);
        return Result.success();
    }


    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        predictService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result getById(@PathVariable Integer id) {

        return Result.success(predictService.getById(id));
    }

    @GetMapping("/totle")
    public Result totle() {

        return Result.success(predictService.predictTotle());
    }

    @GetMapping("/members/{id}")
    public Result members(@PathVariable Integer id) throws IOException {

        return Result.success(predictService.getMalfunctionCount(id));
    }




    @GetMapping("/totle/{id}")
    public Result totle(@PathVariable Integer id) {
        return Result.success(predictService.getCountByFileId(id));
    }

    @GetMapping("/{jsonUrl}")
    public void download(@PathVariable String jsonUrl, HttpServletResponse response) throws IOException {
        predictService.downloadResultFile(jsonUrl, response);
    }
}
```

​	这段 Java 代码定义了一个名为`PredictController`的控制器类，主要用于处理与预测相关的业务逻辑和 HTTP 请求。

`	PredictController`类提供了一系列与预测相关的 RESTful 接口，包括文件上传、开始预测、分页查询、删除数据、获取数据详情和总数等操作，实现了对预测数据的全面管理和处理。



#### 20.com.tps.springbot-controller-RoleController

```
package com.tps.springboot.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Role;
import com.tps.springboot.service.IRoleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private IRoleService roleService;

    // 新增或者更新
    @PostMapping
    public Result save(@RequestBody Role role) {
        roleService.saveOrUpdate(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        roleService.removeById(id);
        return Result.success();
    }
    @GetMapping("/totle")
    public Result totle() {
        LambdaQueryWrapper<Role> roleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        long count = roleService.count(roleLambdaQueryWrapper);
        System.out.println(count);
        return Result.success(count);
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        roleService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    public Result findAll() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(roleService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name);
        queryWrapper.orderByDesc("id");
        return Result.success(roleService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 绑定角色和菜单的关系
     * @param roleId 角色id
     * @param menuIds 菜单id数组
     * @return
     */
    @PostMapping("/roleMenu/{roleId}")
    public Result roleMenu(@PathVariable Integer roleId, @RequestBody List<Integer> menuIds) {
        roleService.setRoleMenu(roleId, menuIds);
        return Result.success();
    }

    @GetMapping("/roleMenu/{roleId}")
    public Result getRoleMenu(@PathVariable Integer roleId) {
        return Result.success( roleService.getRoleMenu(roleId));
    }

}
```

​	这段 Java 代码定义了一个名为`RoleController`的控制器类，主要用于处理与角色（`Role`）相关的业务逻辑和 HTTP 请求。

`	RoleController`类提供了一系列与角色管理相关的 RESTful 接口，包括角色的增删改查、批量操作，以及角色和菜单关系的绑定与查询，实现了对角色数据的全面管理和处理。



#### 21.com.tps.springbot-controller-TeainController

```
package com.tps.springboot.controller;

import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Files;
import com.tps.springboot.service.ITrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 文件上传相关接口
 */
@RestController
//@RequestMapping("/train")
@RequestMapping("/python")
@Transactional
public class TrainController  {

    @Autowired
    private ITrainService trainService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


//    @Resource
//    private SystemConfigUtils config;
    /**
     * 文件上传接口
     *
     * @param file 前端传递过来的文件
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result uploadTrainFile(@RequestParam MultipartFile file) throws Exception {

        if(null == file)
        {
            return Result.error(Constants.CODE_400,"上传的文件为空,请上传文件！");
        }

        trainService.uploadTrainFile(file);

        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

    /***
     * 训练接口
     * @param url
     * @return
     * @throws IOException
     */
    //@Async
    @GetMapping("/getUrl/{url}/{type}")
    public Result beginTrain(@PathVariable String url, @PathVariable String type) throws IOException {
        System.out.println("***********URL********");
        System.out.println(url);
        System.out.println("***********type********");
        System.out.println(type);
        System.out.println("开始多线程,这是模型训练********");
        long statTime = System.currentTimeMillis();

        Result result = trainService.train(url, type);

        System.out.println("训练成功");

        flushRedis(Constants.FILES_KEY);
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("执行时长：%d 毫秒.", (etime - statTime));
        System.out.println("结束多线程,模型训练结束");
        return result;
    }


    /***
     * 模型评估接口
     * @param url
     * @return
     * @throws IOException
     */
    //@Async
    @GetMapping("/analyze/{url}")
    public Result beginAnalyze(@PathVariable String url) throws IOException {
        System.out.println("***********URL********");
        System.out.println(url);
        System.out.println("开始多线程,这是模型评估********");
        long statTime = System.currentTimeMillis();

        Result result = trainService.analyze(url);

        System.out.println("评估成功");

        flushRedis(Constants.FILES_KEY);
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("执行时长：%d 毫秒.", (etime - statTime));
        System.out.println("结束多线程,评估报告生成结束");
        return result;
    }

    // 删除缓存
    private void flushRedis(String key) {
        stringRedisTemplate.delete(key);
    }


    /**
     * 文件下载接口   http://localhost:9090/file/{fileUUID}
     *
     * @param pythonUrl
     * @param response
     * @throws IOException
     */
    @GetMapping("/**/{pythonUrl}")
    public void download(@PathVariable String pythonUrl, HttpServletResponse response) throws IOException {
       trainService.downloadTrainFile(pythonUrl, response);
    }

    @GetMapping("/download")
    public void downloadreport(HttpServletResponse response) throws IOException {
        trainService.downloadAnalyzeFile(response);
    }



    //    @CachePut(value = "files", key = "'frontAll'")
    @PostMapping("/update")
    public Result update(@RequestBody Files files) {
        trainService.updateById(files);
        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

}
```

​	这段 Java 代码定义了一个名为`TrainController`的控制器类，主要用于处理与训练相关的文件上传、模型训练、模型评估以及文件下载等操作。

​	`TrainController`类提供了一套完整的训练相关操作接口，包括文件上传、模型训练、模型评估、文件下载和文件信息更新等功能，并通过 Redis 缓存管理来提高数据的访问性能和一致性。



#### 22.com.tps.springbot-controller-UserController

```
package com.tps.springboot.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.User;
import com.tps.springboot.service.IRoleService;
import com.tps.springboot.service.IUserService;
import com.tps.springboot.controller.dto.UserDTO;
import com.tps.springboot.controller.dto.UserPasswordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;



@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${files.upload.path}")
    private String filesUploadPath;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;
    @Resource
    private IRoleService roleService;
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        UserDTO dto = userService.login(userDTO);
        stringRedisTemplate.opsForValue().set("userId", String.valueOf(dto.getId()));
        return Result.success(dto);
    }
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        return Result.success(userService.register(userDTO));
    }
    // 新增或者更新
    @PostMapping("/saveUpdateUser")
    public Result saveUpdateUser(@RequestBody User user) {
        userService.saveUpdateUser(user);
        return Result.success();
    }


    /**
     * 修改密码
     * @param userPasswordDTO
     * @return
     */
    @PostMapping("/password")
    public Result password(@RequestBody UserPasswordDTO userPasswordDTO) {
        userPasswordDTO.setPassword(SecureUtil.md5(userPasswordDTO.getPassword()));
        userPasswordDTO.setNewPassword(SecureUtil.md5(userPasswordDTO.getNewPassword()));
        userService.updatePassword(userPasswordDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    @GetMapping("/role/{role}")
    public Result findUsersByRole(@PathVariable String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role);
        List<User> list = userService.list(queryWrapper);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result findByUsername(@PathVariable String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return Result.success(userService.getOne(queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam(defaultValue = "") String username,
                               @RequestParam(defaultValue = "") String email,
                               @RequestParam(defaultValue = "") String address) {

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderByDesc("id");
//        if (!"".equals(username)) {
//            queryWrapper.like("username", username);
//        }
//        if (!"".equals(email)) {
//            queryWrapper.like("email", email);
//        }
//        if (!"".equals(address)) {
//            queryWrapper.like("address", address);
//        }
        return Result.success(userService.findPage(new Page<>(pageNum, pageSize), username, email, address));
    }


    @GetMapping("/getUserName")
    public Result getUserName() {
        return Result.success(userService.list());
    }
    @GetMapping("/totle")
    public Result totle() {
        List<User> list = userService.list();
        for (User user : list) {
            Date createTime = user.getCreateTime();

        }
        return Result.success(list.size());
    }
    /**
     * 导出接口
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<User> list = userService.list();
        // 通过工具类创建writer 写出到磁盘路径
//        ExcelWriter writer = ExcelUtil.getWriter(filesUploadPath + "/用户信息.xlsx");
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("address", "地址");
        writer.addHeaderAlias("createTime", "创建时间");
        writer.addHeaderAlias("avatarUrl", "头像");

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }
    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 方式1：(推荐) 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
//        List<User> list = reader.readAll(User.class);
        // 方式2：忽略表头的中文，直接读取表的内容
        List<List<Object>> list = reader.read(1);
        List<User> users = CollUtil.newArrayList();
        for (List<Object> row : list) {
            User user = new User();
            user.setUsername(row.get(0).toString());
            user.setPassword(row.get(1).toString());
            user.setNickname(row.get(2).toString());
            user.setEmail(row.get(3).toString());
            user.setPhone(row.get(4).toString());
            user.setAddress(row.get(5).toString());
            user.setAvatarUrl(row.get(6).toString());
            users.add(user);
        }

        userService.saveBatch(users);
        return Result.success(true);
    }

}
```

​	这段 Java 代码定义了一个名为`UserController`的控制器类，主要用于处理与用户（`User`）相关的业务逻辑和 HTTP 请求，包括用户的登录、注册、信息管理、密码修改、数据的增删改查以及 Excel 数据的导入导出等功能。

​	`UserController`类提供了一套完整的用户管理接口，涵盖了用户的认证、信息管理、数据的增删改查以及 Excel 数据的导入导出等功能，实现了对用户数据的全面管理和处理。



## 四、com.tps.springbot-entity

#### 23.com.tps.springbot-entity-Dict

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("sys_dict")
@Data
public class Dict {

    private String name;
    private String value;
    private String type;

}
```

​	这段 Java 代码定义了一个名为`Dict`的实体类，该类用于表示系统中的字典数据。

​	`Dict`类是一个简单的数据实体类，用于在 Java 程序中表示数据库中`sys_dict`表的记录，通过 MyBatis - Plus 和 Lombok 的注解简化了数据库映射和代码编写，方便进行数据的存储和处理。 由于你提供了三段相同的代码，它们的功能和含义都是一致的。



#### 24.com.tps.springbot-entity-Files

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_trainfile")
public class Files {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String type;
    private Long size;
    //下载链接
    private String url;
    private String pythonurl;
    private String md5;
    private Boolean isDelete;
    private Boolean enable;
    @ApiModelProperty("创建时间")
    private Date createTime;
    @TableField("user_id")
    private int userid;
    private String modeltype;
    private String report;
}
```

​	这段 Java 代码定义了一个名为`Files`的实体类，用于表示系统中与训练文件相关的数据，并通过一些注解来描述实体类与数据库表之间的映射关系以及其他属性。

`	Files`类是一个用于映射数据库表`sys_trainfile`的实体类，通过注解和成员变量定义了文件相关的各种属性，方便在 Java 程序中进行数据的存储、读取和处理。



#### 25.com.tps.springbot-entity-Menu

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@TableName("sys_menu")
@ApiModel(value = "Menu对象", description = "")
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("路径")
    private String path;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("描述")
    private String description;

    @TableField(exist = false)
    private List<Menu> children;

    private Integer pid;

    private String pagePath;
    private String sortNum;


}
```

​	这段 Java 代码定义了一个名为`Menu`的实体类，用于表示系统中的菜单信息。

​	`	Menu`类是一个用于映射数据库表`sys_menu`的实体类，通过注解和成员变量定义了菜单相关的各种属性，包括菜单的基本信息、层级关系和其他辅助信息，方便在 Java 程序中进行菜单数据的存储、读取和处理，同时也为生成 API 文档提供了必要的描述信息。



#### 26.com.tps.springbot-entity-Message

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
@TableName("sys_message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String type;

    private String content;

    @ApiModelProperty("创建时间")
    private Date createTime;

    private Integer sendUserId;

    @TableField("send_user_name")
    private String sendUserName;

    private String sendRealName;

    @TableField("receive_user_name")
    private String receiveUserName;

}
```

​	这段 Java 代码定义了一个名为`Message`的实体类，用于表示系统中的消息数据。

​	`Message`类是一个用于映射数据库表`sys_message`的实体类，通过注解和成员变量定义了消息相关的各种属性，方便在 Java 程序中进行消息数据的存储、读取和处理，同时也为生成 API 文档提供了必要的字段描述信息。



#### 27.com.tps.springbot-entity-OnlineDate

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_result")
public class OnlineDate {
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 测试集中的index
     */
    private Integer testid;
    private Integer result;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @TableField("testfile_id")
    private Integer testfileid;
}
```

​	这段 Java 代码定义了一个名为`OnlineDate`的实体类，用于表示系统中与在线数据相关的信息，通常可能是测试结果相关的数据。

​	`OnlineDate`类是一个用于映射数据库表`sys_result`的实体类，通过注解和成员变量定义了与在线测试数据相关的各种属性，方便在 Java 程序中进行数据的存储、读取和处理，同时也为生成 API 文档提供了必要的字段描述信息。



#### 28.com.tps.springbot-entity-Role

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@TableName("sys_role")
@ApiModel(value = "Role对象", description = "")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("唯一标识")
    private String flag;


}
```

​	这段 Java 代码定义了一个名为`Role`的实体类，用于表示系统中的角色信息。

​	`Role`类是一个用于映射数据库表`sys_role`的实体类，通过注解和成员变量定义了角色相关的各种属性，方便在 Java 程序中进行角色数据的存储、读取和处理，同时也为生成 API 文档提供了必要的描述信息。



#### 29.com.tps.springbot-entity-RoleMenu

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("sys_role_menu")
@Data
public class RoleMenu {

    private Integer roleId;
    private Integer menuId;

}
```

​	这段 Java 代码定义了一个名为`RoleMenu`的实体类，主要用于表示系统中角色和菜单之间的关联关系。

​	`RoleMenu`类是一个用于映射数据库表`sys_role_menu`的实体类，通过两个成员变量`roleId`和`menuId`定义了角色和菜单之间的关联关系，方便在 Java 程序中进行角色和菜单关联数据的存储、读取和处理。



#### 30.com.tps.springbot-entity-TestFiles

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
@TableName("sys_testfile")
public class TestFiles {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String type;
    private Long size;
    private String url;
    private String enable;
    private String md5;
    private Boolean isDelete;
    @ApiModelProperty("创建时间")
    private Date createTime;
    @TableField("user_id")
    private int userid;

    @TableField("jsonUrl")
    private String jsonUrl;
}
```

​	这段 Java 代码定义了一个名为`TestFiles`的实体类，该类用于表示系统中的测试文件相关信息，并且通过一系列注解将其与数据库表`sys_testfile`进行映射。

​	`TestFiles`类是一个用于映射数据库表`sys_testfile`的实体类，借助注解和成员变量定义了测试文件相关的各种属性，方便在 Java 程序中对测试文件数据进行存储、读取和处理，同时也为生成 API 文档提供了必要的字段描述信息。



#### 31.com.tps.springbot-entity-User

```
package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "User对象", description = "")
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("电话")
    private String phone;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("头像")
    private String avatarUrl;

    @ApiModelProperty("角色")
    private String role;

    @ApiModelProperty("roleId编号外键")
    private Integer roleid;


}
```

​	这段 Java 代码定义了一个名为`User`的实体类，用于表示系统中的用户信息。

​	`User`类是一个用于映射数据库表`sys_user`的实体类，通过注解和成员变量定义了用户的各种属性，方便在 Java 程序中进行用户数据的存储、读取和处理，同时为生成 API 文档提供了必要的描述信息。



## 五、com.tps.springbot-exception

#### 32.com.tps.springbot-exception-GlobaIExceptionHandler

```
package com.tps.springboot.exception;

import com.tps.springboot.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 如果抛出的的是ServiceException，则调用该方法
     * @param se 业务异常
     * @return Result
     */


    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handle(ServiceException se){
        return Result.error(se.getCode(), se.getMessage());
    }

}
```

​	这段 Java 代码定义了一个全局异常处理类`GlobalExceptionHandler`，其作用是捕获并处理 Spring Boot 应用程序中抛出的特定类型异常，将异常信息以统一的结果格式返回给客户端。

​	`	GlobalExceptionHandler`类通过`@ControllerAdvice`和`@ExceptionHandler`注解实现了对`ServiceException`类型异常的全局处理，将异常信息以统一的`Result`对象格式返回给客户端，提高了应用程序的健壮性和可维护性。



#### 33.com.tps.springbot-exception-ServiceExcepyion

```
package com.tps.springboot.exception;

import lombok.Getter;

/**
 * 自定义异常
 */
@Getter
public class ServiceException extends RuntimeException {
    private String code;

    public ServiceException(String code, String msg) {
        super(msg);
        this.code = code;
    }

}
```

​	这段 Java 代码定义了一个自定义异常类`ServiceException`，它继承自`RuntimeException`。自定义异常类在 Java 开发中非常有用，尤其是在业务逻辑中出现特定错误时，能够抛出具有特定含义的异常，方便进行错误处理和调试。

​	`ServiceException`类是一个自定义的非检查型异常类，用于在业务逻辑中抛出具有特定错误码和错误信息的异常。通过使用自定义异常类，可以更好地组织和处理业务逻辑中的错误，提高代码的可读性和可维护性。



## 六、com.tps.springbot-mapper

#### 34.com.tps.springbot-mapper-DictMapper

```
package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.Dict;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictMapper extends BaseMapper<Dict> {
}
```

​	这段 Java 代码定义了一个名为`DictMapper`的接口，它是 MyBatis - Plus 框架中的数据访问层组件，主要用于对`Dict`实体类对应数据库表进行数据操作。

​	`DictMapper`接口是一个 MyBatis - Plus 的 Mapper 接口，它借助继承`BaseMapper<Dict>`为`Dict`实体类提供了基本的数据库操作功能，方便在业务逻辑中对`sys_dict`表进行数据的增删改查操作。



#### 35.com.tps.springbot-mapper-FileMapper

```
package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.Files;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<Files> {
}
```

​	这段 Java 代码定义了一个名为`FileMapper`的接口，它是基于 MyBatis - Plus 框架的数据访问层接口，主要用于对`Files`实体类对应的数据库表进行操作。

​	`FileMapper`接口是一个 MyBatis - Plus 的 Mapper 接口，它借助继承`BaseMapper<Files>`为`Files`实体类提供了基本的数据库操作功能，便于在业务逻辑中对`sys_trainfile`表进行数据的增删改查操作。



#### 36.com.tps.springbot-mapper-MenuMapper

```
package com.tps.springboot.mapper;

import com.tps.springboot.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

}
```

​	这段 Java 代码定义了一个名为`MenuMapper`的接口，它是 MyBatis - Plus 框架中的数据访问对象（DAO）接口，用于与数据库中`sys_menu`表进行交互。

​	`MenuMapper`接口借助 MyBatis - Plus 的`BaseMapper`接口，为`Menu`实体类提供了基本的数据库增删改查功能，方便在业务逻辑中对`sys_menu`表进行数据操作。



#### 37.com.tps.springbot-mapper-MessageMapper

```
package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.Message;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    int insertSelective(Message record);
}
```

​	这段 Java 代码定义了一个名为`MessageMapper`的接口，它是基于 MyBatis - Plus 框架的数据访问层接口，用于对`Message`实体类对应的数据库表`sys_message`进行操作。

​	`MessageMapper`接口是一个 MyBatis - Plus 的 Mapper 接口，它继承了`BaseMapper`的基本数据库操作方法，同时定义了一个自定义的插入方法`insertSelective`，用于更灵活地插入消息记录到`sys_message`表中。



#### 38.com.tps.springbot-mapper-ResultMapper

```
package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.OnlineDate;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface ResultMapper extends BaseMapper<OnlineDate> {
}
```

​	这段 Java 代码定义了一个名为`ResultMapper`的接口，其主要功能是与数据库中`sys_result`表进行交互，从而对`OnlineDate`实体类对应的数据进行操作。

​	`ResultMapper`接口借助 MyBatis - Plus 的`BaseMapper`接口，为`OnlineDate`实体类对应的`sys_result`表提供了基本的数据库增删改查功能，方便在业务逻辑中对在线数据进行操作。



#### 39.com.tps.springbot-mapper-RoleMapper

```
package com.tps.springboot.mapper;

import com.tps.springboot.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("select id from sys_role where flag = #{flag}")
    Integer selectByFlag(@Param("flag") String flag);
}
```

​	这段 Java 代码定义了一个名为`RoleMapper`的接口，它是基于 MyBatis - Plus 框架的数据访问层接口，用于对`Role`实体类对应的数据库表`sys_role`进行操作。

​	`RoleMapper`接口是一个 MyBatis - Plus 的 Mapper 接口，它继承了`BaseMapper`的基本数据库操作方法，同时定义了一个自定义的查询方法`selectByFlag`，用于根据角色的唯一标识`flag`查询角色的 ID。



#### 40.com.tps.springbot-mapper-RoleMenuMapper

```
package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    @Delete("delete from sys_role_menu where role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Integer roleId);

    @Select("select menu_id from sys_role_menu where role_id = #{roleId}")
    List<Integer> selectByRoleId(@Param("roleId")Integer roleId);

}
```

​	这段 Java 代码定义了一个名为`RoleMenuMapper`的接口，它基于 MyBatis - Plus 框架，用于对`RoleMenu`实体类对应的数据库表`sys_role_menu`进行数据操作。

​	`RoleMenuMapper`接口不仅继承了`BaseMapper`的基本数据库操作方法，还定义了两个自定义的数据库操作方法：`deleteByRoleId`用于根据角色 ID 删除角色和菜单的关联记录，`selectByRoleId`用于根据角色 ID 查询关联的菜单 ID 列表。



#### 41.com.tps.springbot-mapper-TestFileMapper

```
package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.TestFiles;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestFileMapper extends BaseMapper<TestFiles> {
}
```

​	这段 Java 代码定义了一个名为`TestFileMapper`的接口，它是基于 MyBatis - Plus 框架的数据访问层接口，用于对`TestFiles`实体类对应的数据库表`sys_testfile`进行操作。

​	`TestFileMapper`接口是一个 MyBatis - Plus 的 Mapper 接口，它借助继承`BaseMapper<TestFiles>`为`TestFiles`实体类提供了基本的数据库操作功能，方便在业务逻辑中对`sys_testfile`表进行数据的增删改查操作。



#### 42.com.tps.springbot-mapper-UserMapper

```
package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.controller.dto.UserPasswordDTO;
import com.tps.springboot.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("update sys_user set password = #{newPassword} where username = #{username} and password = #{password}")
    int updatePassword(UserPasswordDTO userPasswordDTO);

    Page<User> findPage(Page<User> page, @Param("username") String username, @Param("email") String email, @Param("address") String address);
}
```

​	这段 Java 代码定义了一个名为`UserMapper`的接口，它是基于 MyBatis - Plus 框架的数据访问层接口，用于对`User`实体类对应的数据库表`sys_user`进行操作。

​	`UserMapper`接口不仅继承了`BaseMapper`的基本数据库操作方法，还定义了两个自定义的数据库操作方法：`updatePassword`用于修改用户密码，`findPage`用于分页查询用户记录。



## 七、com.tps.springbot-service

#### 43.com.tps.springbot-service-impl-MenuServicelmpl

```
package com.tps.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tps.springboot.entity.Menu;
import com.tps.springboot.mapper.MenuMapper;
import com.tps.springboot.service.IMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    @Override
    public List<Menu> findMenus(String name) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_num");
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        // 查询所有数据
        List<Menu> list = list(queryWrapper);
        // 找出pid为null的一级菜单
        List<Menu> parentNodes = list.stream().filter(menu -> menu.getPid() == null).collect(Collectors.toList());
        // 找出一级菜单的子菜单
        for (Menu menu : parentNodes) {
            // 筛选所有数据中pid=父级id的数据就是二级菜单
            menu.setChildren(list.stream().filter(m -> menu.getId().equals(m.getPid())).collect(Collectors.toList()));
        }
        return parentNodes;
    }
}
```

​	这段 Java 代码定义了一个名为`MenuServiceImpl`的服务实现类，它实现了`IMenuService`接口，主要用于处理菜单相关的业务逻辑。

​	`MenuServiceImpl`类实现了`IMenuService`接口中的`findMenus`方法，该方法根据菜单名称进行模糊查询，并将查询结果组织成树形结构，返回一级菜单及其子菜单列表。



#### 44.com.tps.springbot-service-impl-MessageServicelmpl

```
package com.tps.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tps.springboot.entity.Message;
import com.tps.springboot.entity.User;
import com.tps.springboot.mapper.MessageMapper;
import com.tps.springboot.mapper.UserMapper;
import com.tps.springboot.service.MessageService;
import com.tps.springboot.utils.TokenUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void saveMessage(Message message) {
        User currentUser = TokenUtils.getCurrentUser();
        message.setSendUserId(currentUser.getId());
        message.setSendUserName(currentUser.getUsername());
        message.setSendRealName(currentUser.getNickname());
        messageMapper.insert(message);
    }

    @Override
    public void updateMessage(Message message) {
        LambdaQueryWrapper<Message> LambdaqueryWrapper = new LambdaQueryWrapper<>();
        LambdaqueryWrapper.eq(Message::getId,message.getId());
        Message newMessage = messageMapper.selectOne(LambdaqueryWrapper);

        User currentUser = TokenUtils.getCurrentUser();
        newMessage.setSendUserId(currentUser.getId());

        newMessage.setSendUserName(currentUser.getUsername());
        newMessage.setSendRealName(currentUser.getNickname());

        newMessage.setTitle(message.getTitle());
        newMessage.setContent(message.getContent());
        newMessage.setType(message.getType());
        newMessage.setReceiveUserName(message.getReceiveUserName());
        messageMapper.updateById(newMessage);
    }
}
```

​	这段 Java 代码定义了一个名为`MessageServiceImpl`的服务实现类，它实现了`MessageService`接口，主要负责处理消息相关的业务逻辑，如保存消息和更新消息。

​	`MessageServiceImpl`类实现了`MessageService`接口中的`saveMessage`和`updateMessage`方法，分别用于保存和更新消息记录，并在操作过程中自动设置消息的发送者信息。



#### 45.com.tps.springbot-service-impl-PredictServicelmpl

```
package com.tps.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.OnlineDate;
import com.tps.springboot.entity.TestFiles;
import com.tps.springboot.mapper.ResultMapper;
import com.tps.springboot.mapper.TestFileMapper;
import com.tps.springboot.service.IPredictService;
import com.tps.springboot.utils.FileUtils;
import com.tps.springboot.utils.JsonUtils;
import com.tps.springboot.utils.PythonUtils;
import com.tps.springboot.utils.SystemConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@Service
public class PredictServiceImpl  implements IPredictService {

    @Resource
    private SystemConfigUtils config;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Logger logger = LoggerFactory.getLogger(PredictServiceImpl.class);

    @Resource
    private TestFileMapper testFileMapper;

    @Resource
    private ResultMapper resultMapper;


    @Override
    public boolean uploadPredictFile(MultipartFile file) throws Exception {

        logger.info("#######################开始将预测集文件写入#################");
        String fileName = FileUtils.writeFile(config.getPredictFilePath(),file);

        testFileMapper.insert(buildPredictFile(file, fileName));

        flushRedis(Constants.FILES_KEY);
        return true;
    }

    @Override
    public Result beginPredict(String url) throws IOException {
        // 执行时间（1s）
        TestFiles testFiles = selectTestFilesByUrl(url);
        if (!ObjectUtil.isEmpty(testFiles.getEnable())){
            return Result.error("505","已完成，请查看结果");
        }

        //读取文件开始训练
        String predictResultFileName = url.replace("csv", "json");

        try {

            String[] arguments = new String[] {config.getPythonInterpreter(),
                    config.getPythonPredictCode(),
                    config.getPredictFilePath()+url,
                    config.getPredictFilePath()+predictResultFileName,
                    config.getPredictModelPath()
            };

            int i = PythonUtils.trainByPython(arguments);

            if (i == 1) {
                return Result.error("507", "训练失败");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("预测成功");
        testFiles.setEnable("1");
        testFiles.setJsonUrl(predictResultFileName);
        testFileMapper.updateById(testFiles);

        //读取json，解析
      HashMap<String,Integer> resultMap = (HashMap<String, Integer>) JsonUtils.readJsonFile
              (config.getPredictFilePath()+predictResultFileName);
        //将本次结果入库 sys_result
        resultMap.forEach((key, value) -> {
            OnlineDate record = new OnlineDate();
            record.setCreateTime(new Date());
            record.setTestfileid(testFiles.getId());
            record.setTestid(Integer.valueOf(key));
            record.setResult(value);
            resultMapper.insert(record);
        });

        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

    @Override
    public IPage<TestFiles> findPage(Integer pageNum, Integer pageSize, String name) {
        QueryWrapper<TestFiles> queryWrapper = new QueryWrapper<>();

        // 查询未删除的记录
        queryWrapper.eq("is_delete", false);
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }

       IPage<TestFiles> result = testFileMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        return result;
    }



    private TestFiles selectTestFilesByUrl(String url) {
        LambdaQueryWrapper<TestFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TestFiles::getUrl,"http://localhost:9090/DataTest/"+url);
        return testFileMapper.selectOne(queryWrapper);
    }

    private TestFiles buildPredictFile(MultipartFile file, String fileName) throws IOException {

        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();
        // 获取文件的md5
        String md5 = null;//SecureUtil.md5(file.getInputStream());

        String url = "http://" + config.getServerIp() + ":9090/DataTest/" + fileName;
        //获取当前用户的user_id
        String userId = stringRedisTemplate.opsForValue().get("userId");

        System.out.println("----------------------------------"+userId);
        // 存储数据库
        TestFiles saveFile = new TestFiles();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size/1024); // 单位 kb
        saveFile.setUrl(url);
        saveFile.setMd5(md5);
        saveFile.setUserid(Integer.parseInt(userId));

        return saveFile;

    }


    // 设置缓存
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 删除缓存
    private void flushRedis(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean deletePredictData(Integer id) {

        LambdaQueryWrapper<OnlineDate> onlineDateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        onlineDateLambdaQueryWrapper.eq(OnlineDate::getTestfileid,id);
        resultMapper.delete(onlineDateLambdaQueryWrapper);
        testFileMapper.deleteById(id);
        flushRedis(Constants.FILES_KEY);
        return true;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {

        QueryWrapper<OnlineDate> onlineDateQueryWrapper = new QueryWrapper<>();
        onlineDateQueryWrapper.in("testfile_id",ids);
        List<OnlineDate> onlinedates = resultMapper.selectList(onlineDateQueryWrapper);
        for (OnlineDate onlinedate:onlinedates){
            resultMapper.deleteById(onlinedate);
        }
        // select * from sys_file where id in (id,id,id...)
        QueryWrapper<TestFiles> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<TestFiles> testfiles = testFileMapper.selectList(queryWrapper);
        for (TestFiles file : testfiles) {
            testFileMapper.deleteById(file);
        }
        return true;
    }

    @Override
    public TestFiles getById(Integer id) {

        return testFileMapper.selectById(id);
    }

    @Override
    public Integer predictTotle() {

        List<TestFiles> testfiles = testFileMapper.selectList(new QueryWrapper<TestFiles>());
        String today = DateUtil.today();
        Integer totle=0;
        for (TestFiles file : testfiles) {
            Date createTime = file.getCreateTime();
            String format = DateUtil.format(createTime, "yyyy-MM-dd");
            if (format.equals(today)){
                totle++;
            }
        }
        return totle;
    }

    @Override
    public  ArrayList<Long> getMalfunctionCount(Integer id) {

        ArrayList<Long> integers = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LambdaQueryWrapper<OnlineDate> dateLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dateLambdaQueryWrapper.eq(OnlineDate::getTestfileid,id);
            dateLambdaQueryWrapper.eq(OnlineDate::getResult, i);
            Long aLong = resultMapper.selectCount(dateLambdaQueryWrapper);
            integers.add(aLong);
        }

        System.out.println(integers.toString());
        return integers;
    }

    @Override
    public Long getCountByFileId(Integer id) {
        LambdaQueryWrapper<OnlineDate> dateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dateLambdaQueryWrapper.eq(OnlineDate::getTestfileid,id);
        return resultMapper.selectCount(dateLambdaQueryWrapper);
    }

    @Override
    public void downloadResultFile(String jsonUrl, HttpServletResponse response) throws IOException {
      FileUtils.downloadResultFile(jsonUrl,config.getPredictFilePath(),response);
    }
}
```

​	这段 Java 代码定义了一个名为`PredictServiceImpl`的服务实现类，它实现了`IPredictService`接口，主要负责处理与预测相关的业务逻辑，包括上传预测文件、开始预测、文件分页查询、删除预测数据、获取预测统计信息以及下载预测结果文件等功能。

​	`PredictServiceImpl`类提供了一套完整的与预测相关的业务逻辑处理方法，涵盖了文件上传、预测执行、数据查询、删除以及统计等功能。



#### 46.com.tps.springbot-service-impl-RoleServicelmpl

```
package com.tps.springboot.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.tps.springboot.entity.Menu;
import com.tps.springboot.entity.Role;
import com.tps.springboot.entity.RoleMenu;
import com.tps.springboot.mapper.RoleMapper;
import com.tps.springboot.mapper.RoleMenuMapper;
import com.tps.springboot.service.IMenuService;
import com.tps.springboot.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private IMenuService menuService;

    @Transactional
    @Override
    public void setRoleMenu(Integer roleId, List<Integer> menuIds) {
//        QueryWrapper<RoleMenu> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("role_id", roleId);
//        roleMenuMapper.delete(queryWrapper);

        // 先删除当前角色id所有的绑定关系
        roleMenuMapper.deleteByRoleId(roleId);

        // 再把前端传过来的菜单id数组绑定到当前的这个角色id上去
        List<Integer> menuIdsCopy = CollUtil.newArrayList(menuIds);
        for (Integer menuId : menuIds) {
            Menu menu = menuService.getById(menuId);
            if (menu.getPid() != null && !menuIdsCopy.contains(menu.getPid())) { // 二级菜单 并且传过来的menuId数组里面没有它的父级id
                // 那么我们就得补上这个父级id
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menu.getPid());
                roleMenuMapper.insert(roleMenu);
                menuIdsCopy.add(menu.getPid());
            }
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }

    @Override
    public List<Integer> getRoleMenu(Integer roleId) {

        return roleMenuMapper.selectByRoleId(roleId);
    }

}
```

​	这段 Java 代码定义了一个名为`RoleServiceImpl`的服务实现类，它实现了`IRoleService`接口，主要负责处理角色与菜单关联的业务逻辑。

​	`RoleServiceImpl`类实现了`IRoleService`接口中的`setRoleMenu`和`getRoleMenu`方法，分别用于设置和获取角色与菜单的关联关系。在设置关联关系时，会先删除该角色原有的关联记录，然后重新绑定新的菜单`id`，同时会自动补全二级菜单的父菜单关联。



#### 47.com.tps.springbot-service-impl-TrainServicelmpl

```
package com.tps.springboot.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Files;
import com.tps.springboot.mapper.FileMapper;
import com.tps.springboot.service.ITrainService;
import com.tps.springboot.utils.FileUtils;
import com.tps.springboot.utils.PythonUtils;
import com.tps.springboot.utils.SystemConfigUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Service
public class TrainServiceImpl  implements ITrainService {


    @Resource
    private SystemConfigUtils config;

    @Resource
    private FileMapper fileMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Logger logger = LoggerFactory.getLogger(TrainServiceImpl.class);

    /**
     * 保存上传文件（比如上传训练集）
     *
     * step1:读取基本信息
     * step2：写文件
     * step3: 将数据写入trainfile文件
     * @param file
     * @return
     */
    @Override
    public boolean uploadTrainFile(MultipartFile file) throws Exception {

        logger.info("#######################开始将训练集文件写入#################");

        String trianFileName = FileUtils.writeFile( config.getTrainFilePath(),file);

        Files saveFileRecord = buildTrainFile(file, trianFileName);
        fileMapper.insert(saveFileRecord);

        return true;
    }

    /**
     * 封装入库的
     * @param file
     * @param trianFileName
     * @return
     */
    private Files buildTrainFile(MultipartFile file,  String trianFileName) throws IOException {
        long size = file.getSize();
        // 存储数据库
        Files saveFile = new Files();
        saveFile.setName(file.getOriginalFilename());
        String type = FileUtil.extName(file.getOriginalFilename());
        saveFile.setType(type);
        // 单位 kb
        saveFile.setSize(size / 1024);
        //1.9090应该存放到配置文件中 2.url返回给前端用下载的训练用的url，可以将名字改成modelDownLoadUrl
        String serverIp = config.getServerIp();
        String url = "http://" + serverIp + ":9090/python/" + trianFileName;
        saveFile.setUrl(url);

        // 获取文件的md5，用于校验文件数据是否被删除或改动
       // String md5 = SecureUtil.md5(file.getInputStream());
        saveFile.setMd5(null);
        //获取当前用户的user_id
        String userId = stringRedisTemplate.opsForValue().get("userId");
        saveFile.setUserid(Integer.parseInt(userId));
        return saveFile;
    }

    /**
     * 训练
     * @param url  训练模型的名字
     * @return
     */
    @Override
    public Result train(String url, String type) throws IOException {
        // 查询是否已经完成训练
        LambdaQueryWrapper<Files> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Files::getUrl, "http://localhost:9090/python/" + url);
        Files files = fileMapper.selectOne(queryWrapper);

        //pythonUrl存在而且当前选择的模型类别符合
        if (!ObjectUtils.isEmpty(files.getPythonurl()) && type.equals(files.getModeltype())) {
            return Result.error("505", "训练已完成，请下载");
        }

        //读取文件开始训练
        String trainModelFileName = url.replace("csv", "h5");
        if(type != null && type.equals("modelDT"))
            trainModelFileName = url.replace("csv", "pkl");
        if(type != null && type.equals("modelRF"))
            trainModelFileName = url.replace("csv", "pkl");

        try {
            String[] arguments = new String[] {config.getPythonInterpreter(),
                    config.getPythonTrianCode(),
                    config.getTrainFilePath()+url,
                    config.getTrainFilePath()+trainModelFileName,type};

            int i = PythonUtils.trainByPython(arguments);

            if (i == 1) {
                return Result.error("507", "训练失败");
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        files.setPythonurl(trainModelFileName);
        files.setModeltype(type);
        fileMapper.updateById(files);

        return Result.success();
    }

    @Override
    public Result analyze(String url) throws IOException{

        // 查询是否已经完成生成报告
        LambdaQueryWrapper<Files> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Files::getUrl, "http://localhost:9090/python/" + url);
        Files files = fileMapper.selectOne(queryWrapper);

        //pythonUrl存在而且当前选择的模型类别符合
        if (!ObjectUtils.isEmpty(files.getReport())) {
            return Result.error("505", "报告已完成，请下载");
        }

        try {
            String[] arguments = new String[] {config.getPythonInterpreter(),
                    config.getPythonAnalyzeCode(),
                    config.getTrainFilePath()+url,
                    config.getModelAnalysisPath()};

            int i = PythonUtils.trainByPython(arguments);

            if (i == 1) {
                return Result.error("507", "生成失败");
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        files.setReport("done");
        fileMapper.updateById(files);

        return Result.success();
    }

    @Override
    public void updateById(Files files) {
        fileMapper.updateById(files);
    }

    @Override
    public void downloadTrainFile( String pythonUrl, HttpServletResponse response) throws IOException {
        FileUtils.downloadResultFile(pythonUrl,config.getTrainFilePath(),response);
    }

    @Override
    public void downloadAnalyzeFile(HttpServletResponse response) throws IOException {
        FileUtils.downloadAnalyzeFile(response);
    }

}
```

​	这段 Java 代码定义了一个名为`TrainServiceImpl`的服务实现类，它实现了`ITrainService`接口，主要负责处理与训练相关的业务逻辑，包括上传训练文件、进行训练、生成分析报告以及文件下载等功能。

​	`TrainServiceImpl`类提供了一套完整的与训练相关的业务逻辑处理方法，涵盖了文件上传、训练、生成报告以及文件下载等功能，并且在操作过程中会与数据库和 Redis 进行交互，保证数据的一致性和完整性。



#### 48.com.tps.springbot-service-impl-UserServicelmpl

```
package com.tps.springboot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.RoleEnum;
import com.tps.springboot.controller.dto.UserDTO;
import com.tps.springboot.controller.dto.UserPasswordDTO;
import com.tps.springboot.entity.Menu;
import com.tps.springboot.entity.Role;
import com.tps.springboot.entity.User;
import com.tps.springboot.exception.ServiceException;
import com.tps.springboot.mapper.RoleMapper;
import com.tps.springboot.mapper.RoleMenuMapper;
import com.tps.springboot.mapper.UserMapper;
import com.tps.springboot.service.IMenuService;
import com.tps.springboot.service.IUserService;
import com.tps.springboot.utils.TokenUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final Log LOG = Log.get();

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private IMenuService menuService;

    @Override
    public UserDTO login(UserDTO userDTO) {
        // 用户密码 md5加密
        userDTO.setPassword(SecureUtil.md5(userDTO.getPassword()));
        User one = getUserInfo(userDTO);
        if (one != null) {
            BeanUtil.copyProperties(one, userDTO, true);
            // 设置token
            String token = TokenUtils.genToken(one.getId().toString(), one.getPassword());
            userDTO.setToken(token);

            String role = one.getRole(); // ROLE_ADMIN
            // 设置用户的菜单列表
            List<Menu> roleMenus = getRoleMenus(role);
            userDTO.setMenus(roleMenus);
            return userDTO;
        } else {
            throw new ServiceException(Constants.CODE_600, "用户名或密码错误");
        }
    }

    @Override
    public User register(UserDTO userDTO) {
        // 用户密码 md5加密
        userDTO.setPassword(SecureUtil.md5(userDTO.getPassword()));
        User one = getUserInfo(userDTO);
        if (one == null) {
            one = new User();
            BeanUtil.copyProperties(userDTO, one, true);
            // 默认一个普通用户的角色
            String role = RoleEnum.ROLE_WORKER.toString();
            LambdaQueryWrapper<Role> roleLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roleLambdaQueryWrapper.eq(Role::getFlag,role);
            Integer roleid = roleMapper.selectOne(roleLambdaQueryWrapper).getId();
            one.setRoleid(roleid);
            one.setRole(role);
            if (one.getNickname() == null) {
                one.setNickname(one.getUsername());
            }
            save(one);  // 把 copy完之后的用户对象存储到数据库
        } else {
            throw new ServiceException(Constants.CODE_600, "用户已存在");
        }
        return one;
    }

    @Override
    public void updatePassword(UserPasswordDTO userPasswordDTO) {
        int update = userMapper.updatePassword(userPasswordDTO);
        if (update < 1) {
            throw new ServiceException(Constants.CODE_600, "密码错误");
        }
    }

    @Override
    public Page<User> findPage(Page<User> page, String username, String email, String address) {
        return userMapper.findPage(page, username, email, address);
    }


    @Override
    public void saveUpdateUser(User user) {
        LambdaQueryWrapper<Role> roleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        String role = user.getRole();
        roleLambdaQueryWrapper.eq(Role::getFlag,role);
        Integer roleid = roleMapper.selectOne(roleLambdaQueryWrapper).getId();
        user.setRoleid(roleid);
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getId,user.getId());
        List<User> users = userMapper.selectList(userLambdaQueryWrapper);
        if (users.size()==0){
            userMapper.insert(user);
        }
        userMapper.updateById(user);
    }


    private User getUserInfo(UserDTO userDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        queryWrapper.eq("password", userDTO.getPassword());
        User one;
        try {
            one = getOne(queryWrapper); // 从数据库查询用户信息
        } catch (Exception e) {
            LOG.error(e);
            throw new ServiceException(Constants.CODE_500, "系统错误");
        }
        return one;
    }

    /**
     * 获取当前角色的菜单列表
     * @param roleFlag
     * @return
     */
    private List<Menu> getRoleMenus(String roleFlag) {
        Integer roleId = roleMapper.selectByFlag(roleFlag);
        // 当前角色的所有菜单id集合
        List<Integer> menuIds = roleMenuMapper.selectByRoleId(roleId);

        // 查出系统所有的菜单(树形)
        List<Menu> menus = menuService.findMenus("");
        // new一个最后筛选完成之后的list
        List<Menu> roleMenus = new ArrayList<>();
        // 筛选当前用户角色的菜单
        for (Menu menu : menus) {
            if (menuIds.contains(menu.getId())) {
                roleMenus.add(menu);
            }
            List<Menu> children = menu.getChildren();
            // removeIf()  移除 children 里面不在 menuIds集合中的 元素
            children.removeIf(child -> !menuIds.contains(child.getId()));
        }
        return roleMenus;
    }



}
```

​	这段 Java 代码定义了一个名为`UserServiceImpl`的服务实现类，它实现了`IUserService`接口，主要负责处理用户相关的业务逻辑，如用户登录、注册、密码修改、分页查询、保存或更新用户信息等。

​	`UserServiceImpl`类实现了用户的登录、注册、密码修改、分页查询、保存或更新用户信息等功能，同时处理了用户角色对应的菜单列表的获取逻辑。在处理过程中，会对用户输入的密码进行加密，对异常情况进行捕获和处理，并使用日志记录器记录日志信息。



#### 49.com.tps.springbot-service-ImenuService

```
package com.tps.springboot.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.User;
import com.tps.springboot.service.IRoleService;
import com.tps.springboot.service.IUserService;
import com.tps.springboot.controller.dto.UserDTO;
import com.tps.springboot.controller.dto.UserPasswordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;



@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${files.upload.path}")
    private String filesUploadPath;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;
    @Resource
    private IRoleService roleService;
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        UserDTO dto = userService.login(userDTO);
        stringRedisTemplate.opsForValue().set("userId", String.valueOf(dto.getId()));
        return Result.success(dto);
    }
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        return Result.success(userService.register(userDTO));
    }
    // 新增或者更新
    @PostMapping("/saveUpdateUser")
    public Result saveUpdateUser(@RequestBody User user) {
        userService.saveUpdateUser(user);
        return Result.success();
    }


    /**
     * 修改密码
     * @param userPasswordDTO
     * @return
     */
    @PostMapping("/password")
    public Result password(@RequestBody UserPasswordDTO userPasswordDTO) {
        userPasswordDTO.setPassword(SecureUtil.md5(userPasswordDTO.getPassword()));
        userPasswordDTO.setNewPassword(SecureUtil.md5(userPasswordDTO.getNewPassword()));
        userService.updatePassword(userPasswordDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    @GetMapping("/role/{role}")
    public Result findUsersByRole(@PathVariable String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role);
        List<User> list = userService.list(queryWrapper);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result findByUsername(@PathVariable String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return Result.success(userService.getOne(queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam(defaultValue = "") String username,
                               @RequestParam(defaultValue = "") String email,
                               @RequestParam(defaultValue = "") String address) {

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderByDesc("id");
//        if (!"".equals(username)) {
//            queryWrapper.like("username", username);
//        }
//        if (!"".equals(email)) {
//            queryWrapper.like("email", email);
//        }
//        if (!"".equals(address)) {
//            queryWrapper.like("address", address);
//        }
        return Result.success(userService.findPage(new Page<>(pageNum, pageSize), username, email, address));
    }


    @GetMapping("/getUserName")
    public Result getUserName() {
        return Result.success(userService.list());
    }
    @GetMapping("/totle")
    public Result totle() {
        List<User> list = userService.list();
        for (User user : list) {
            Date createTime = user.getCreateTime();

        }
        return Result.success(list.size());
    }
    /**
     * 导出接口
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<User> list = userService.list();
        // 通过工具类创建writer 写出到磁盘路径
//        ExcelWriter writer = ExcelUtil.getWriter(filesUploadPath + "/用户信息.xlsx");
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("address", "地址");
        writer.addHeaderAlias("createTime", "创建时间");
        writer.addHeaderAlias("avatarUrl", "头像");

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }
    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 方式1：(推荐) 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
//        List<User> list = reader.readAll(User.class);
        // 方式2：忽略表头的中文，直接读取表的内容
        List<List<Object>> list = reader.read(1);
        List<User> users = CollUtil.newArrayList();
        for (List<Object> row : list) {
            User user = new User();
            user.setUsername(row.get(0).toString());
            user.setPassword(row.get(1).toString());
            user.setNickname(row.get(2).toString());
            user.setEmail(row.get(3).toString());
            user.setPhone(row.get(4).toString());
            user.setAddress(row.get(5).toString());
            user.setAvatarUrl(row.get(6).toString());
            users.add(user);
        }

        userService.saveBatch(users);
        return Result.success(true);
    }

}
```

​	这段 Java 代码定义了一个名为`UserController`的控制器类，用于处理与用户相关的 HTTP 请求，涵盖了用户的登录、注册、信息增删改查、密码修改、数据导出导入等功能。

​	`UserController`类通过各种请求处理方法，实现了对用户信息的全面管理，包括登录、注册、增删改查、密码修改、数据导出导入等功能，并通过与`IUserService`和`IRoleService`的交互，完成具体的业务逻辑处理。



#### 50.com.tps.springbot-service-IPredictService

```
package com.tps.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.TestFiles;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public interface IPredictService {

    boolean uploadPredictFile(MultipartFile file) throws Exception;


    Result beginPredict(String url) throws IOException;

    IPage<TestFiles> findPage(Integer pageNum,
                              Integer pageSize,
                              String name);

    boolean deletePredictData(Integer id);

    boolean deleteBatch( List<Integer> ids);

    TestFiles getById( Integer id);

    /**
     * 当年预测的次数
     * @return
     */
    Integer predictTotle();

    /**
     *
     * @param id
     * @return
     */
    ArrayList<Long> getMalfunctionCount(Integer id);

    /**
     *
     * @param id
     * @return
     */
    Long getCountByFileId(Integer id);

    void downloadResultFile( String jsonUrl, HttpServletResponse response) throws IOException;
}
```

​	这段 Java 代码定义了一个名为`IPredictService`的接口，该接口主要用于处理预测相关的业务逻辑，提供了一系列操作预测文件、执行预测任务、数据分页查询、数据删除、获取预测数据等功能的抽象方法。

​	`IPredictService`接口定义了一系列与预测相关的业务方法，包括文件上传、预测执行、数据查询、数据删除、结果下载等功能，具体的实现需要在对应的服务实现类中完成。



#### 51.com.tps.springbot-service-IRoleService

```
package com.tps.springboot.service;

import com.tps.springboot.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface IRoleService extends IService<Role> {

    void setRoleMenu(Integer roleId, List<Integer> menuIds);

    List<Integer> getRoleMenu(Integer roleId);
}
```

​	这段代码定义了一个名为`IRoleService`的接口，它继承自 MyBatis - Plus 提供的`IService<Role>`接口，用于处理与角色（`Role`）相关的业务逻辑。

​	`IRoleService`接口定义了处理角色与菜单关联关系的业务方法，同时继承了`IService<Role>`接口的通用 CRUD 方法，为角色相关的业务逻辑提供了全面的服务接口。具体的实现需要在对应的服务实现类中完成。



#### 52.com.tps.springbot-service-ITrainService

```
package com.tps.springboot.service;

import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Files;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public interface ITrainService {

    /**
     * 上传的文件写入本地文件夹
     * @param file
     * @return
     * @throws Exception
     */
    boolean uploadTrainFile(MultipartFile file) throws Exception;


    /**
     * 调用
     * @param url
     * @return
     * @throws IOException
     */
    Result train(String url, String type) throws IOException;

    Result analyze(String url) throws IOException;
    /**
     * 修改train文件
     * @param files
     */
    void updateById(Files files);

    void downloadTrainFile(String pythonUrl, HttpServletResponse response) throws IOException;

    void downloadAnalyzeFile(HttpServletResponse response) throws IOException;
}
```

​	这段 Java 代码定义了一个名为`ITrainService`的接口，该接口主要用于处理与训练相关的业务逻辑，提供了一系列操作训练文件、执行训练任务、生成分析报告以及下载相关文件等功能的抽象方法。

​	`ITrainService`接口定义了一系列与训练相关的业务方法，包括文件上传、训练执行、分析报告生成、文件更新和文件下载等功能，具体的实现需要在对应的服务实现类中完成。



#### 53.com.tps.springbot-service-IUserService

```
package com.tps.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.controller.dto.UserDTO;
import com.tps.springboot.controller.dto.UserPasswordDTO;
import com.tps.springboot.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IUserService extends IService<User> {


    UserDTO login(UserDTO userDTO);

    User register(UserDTO userDTO);

    void updatePassword(UserPasswordDTO userPasswordDTO);

    Page<User> findPage(Page<User> objectPage, String username, String email, String address);

    void saveUpdateUser(User user);


}
```

​	这段 Java 代码定义了一个名为`IUserService`的接口，它继承自 MyBatis - Plus 提供的`IService<User>`接口，主要用于处理与用户（`User`）相关的业务逻辑。

​	`IUserService`接口定义了处理用户相关业务逻辑的方法，同时继承了`IService<User>`接口的通用 CRUD 方法，为用户管理提供了全面的服务接口。具体的实现需要在对应的服务实现类中完成。



#### 54.com.tps.springbot-service-MessageService

```
package com.tps.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tps.springboot.entity.Message;


public interface MessageService extends IService<Message> {

    void  saveMessage(Message message);
    void updateMessage(Message message);
}
```

​	这段 Java 代码定义了一个名为`MessageService`的接口，它继承自 MyBatis - Plus 的`IService<Message>`接口，主要用于处理与消息（`Message`）相关的业务逻辑。

​	`MessageService`接口定义了与消息相关的业务操作方法，既继承了`IService`的通用功能，又添加了自定义的保存和更新消息的方法，为消息管理提供了抽象的服务接口，具体的实现需要在对应的服务实现类中完成。



## 八、com.tps.springbot-utils

#### 55.com.tps.springbot-utils-CodeGenerator

```
package com.tps.springboot.utils;


public class CodeGenerator {

    public static void main(String[] args) {

    }


}
```

​	这段 Java 代码定义了一个名为`CodeGenerator`的类，它位于`com.tps.springboot.utils`包下。

​	`CodeGenerator`类目前只是一个空的框架，可用于后续添加代码生成相关的功能。



#### 56.com.tps.springbot-utils-FileUtils

```
package com.tps.springboot.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;


public final  class FileUtils {

    @Value("${files.modelAnalysis.path}")
    private static String filePath;

    static {
        filePath = "D:/newSoftcup/soft-cup/python/model_analysis.docx";
    }


    public static String  writeFile(String path, MultipartFile file) throws Exception {

        String type = FileUtil.extName(file.getOriginalFilename());
        // 定义一个文件唯一的标识码
        String uuid = "";
        synchronized (uuid) {
            uuid = (UUID.randomUUID().toString()).replace("-", "");
        }

        String fileName = uuid + StrUtil.DOT + type;
        File uploadFile = new File(path + fileName);
        // 判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
        File parentFile = uploadFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        try {
            // 上传文件到磁盘
            file.transferTo(uploadFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("写文件失败");
        }
        return fileName;
    }

    public static void downloadResultFile(String fileName,String srcFilePath, HttpServletResponse response) throws IOException {
        System.out.println(fileName);
        // 根据文件的唯一标识码获取文件
        File uploadFile = new File(srcFilePath+fileName);
        System.out.println(uploadFile);
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentType("application/octet-stream");
        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        System.out.println("下载成功");
        os.flush();
        os.close();
    }

    public static void downloadAnalyzeFile(HttpServletResponse response) throws IOException {

        File file = new File(filePath);

        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=model_analysis.docx");
        response.setContentType("application/octet-stream");
        // 读取文件的字节流
        os.write(FileUtil.readBytes(file));
        System.out.println("下载成功");
        os.flush();
        os.close();
    }
}
```

​	这段 Java 代码定义了一个名为`FileUtils`的工具类，它位于`com.tps.springboot.utils`包下，主要用于处理文件的上传和下载操作。

​	`FileUtils`类提供了文件上传和下载的工具方法，方便在 Spring Boot 应用中处理文件的读写操作。



#### 57.com.tps.springbot-utils-JsonUtils

```
package com.tps.springboot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;



public final class JsonUtils {



    public static Object readJsonFile(String filePath) throws IOException {

        File file = new File(filePath);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, Object.class);

        //遍历
//        myObject.forEach((key, value) -> {
//            System.out.println(key);
//            System.out.println(value);
//        });
    }
}
```

​	这段 Java 代码定义了一个名为`JsonUtils`的工具类，它位于`com.tps.springboot.utils`包下，主要功能是读取指定路径的 JSON 文件，并将其内容解析为 Java 对象。

​	`JsonUtils`类的`readJsonFile`方法提供了一个简单的工具，用于读取指定路径的 JSON 文件并将其内容解析为 Java 对象。



#### 58.com.tps.springbot-utils-PythonUtils

```
package com.tps.springboot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public final class PythonUtils {


    /**
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int trainByPython(String[] arguments) throws IOException,InterruptedException{

        //String[] arguments = new String[] {pythonInterpreter,CodePath,saveFilePath,fileName};


        Process process = Runtime.getRuntime().exec(arguments);
        //获取 Python 脚本的标准输出和标准错误输出
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();

        //创建读取器并将其分别注册到标准输出和标准错误输出流上
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
        String line = null;
        System.out.println("标准输出:");
        while ((line = stdoutReader.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("标准错误输出:");
        while ((line = stderrReader.readLine()) != null) {
            System.err.println(line);
        }


//        java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
//         返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
        int re = process.waitFor();
        return re;
    }

}
```

​	这段 Java 代码定义了一个名为`PythonUtils`的工具类，位于`com.tps.springboot.utils`包下，其主要功能是在 Java 程序中调用 Python 脚本并执行训练任务，同时捕获 Python 脚本的标准输出和标准错误输出，最后返回 Python 脚本的执行结果。

​	`PythonUtils`类的`trainByPython`方法提供了一种在 Java 程序中调用 Python 脚本并获取执行结果的方式，同时可以捕获并输出脚本的标准输出和标准错误信息。



#### 59.com.tps.springbot-utils-SystemConfigUtils

```
package com.tps.springboot.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
@Data
public class SystemConfigUtils {

    @Value("${files.trainFile.path}")
    private String trainFilePath;

    @Value("${files.predictFile.path}")
    private String predictFilePath;

    @Value("${server.ip}")
    private String serverIp;

    @Value("${files.pythonInterpreter.path}")
    private String pythonInterpreter;

    @Value("${files.pythonTrianCode.path}")
    private String pythonTrianCode;

    @Value("${files.pythonPredictCode.path}")
    private String pythonPredictCode;


    @Value("${files.predictModel.path}")
    private String predictModelPath;

    @Value("${files.modelAnalysis.path}")
    private String modelAnalysisPath;

    @Value("${files.pythonAnalyzeCode.path}")
    private String pythonAnalyzeCode;

}
```

​	这段 Java 代码定义了一个名为`SystemConfigUtils`的类，它位于`com.tps.springboot.utils`包下，主要用于读取 Spring Boot 应用配置文件中的配置项，并将这些配置项封装到类的属性中，方便在应用的其他地方使用。

​	`SystemConfigUtils`类通过`@Component`注解将自身注册为 Spring 组件，使用`@Data`注解简化代码，通过`@Value`注解从配置文件中读取系统配置信息，并将这些信息封装到类的属性中，方便在应用的其他地方使用这些配置。



#### 60.com.tps.springbot-utils-TokenUtils

```
package com.tps.springboot.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.tps.springboot.entity.User;
import com.tps.springboot.service.IUserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class TokenUtils {

    private static IUserService staticUserService;

    @Resource
    private IUserService userService;

    @PostConstruct
    public void setUserService() {
        staticUserService = userService;
    }

    /**
     * 生成token
     *
     * @return
     */
    public static String genToken(String userId, String sign) {
        return JWT.create().withAudience(userId) // 将 user id 保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // 2小时后token过期
                .sign(Algorithm.HMAC256(sign)); // 以 password 作为 token 的密钥
    }

    /**
     * 获取当前登录的用户信息
     *
     * @return user对象
     */
    public static User getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            System.out.println("=================="+token);
            if (StrUtil.isNotBlank(token)) {
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserService.getById(Integer.valueOf(userId));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
```

​	这段 Java 代码定义了一个名为`TokenUtils`的工具类，位于`com.tps.springboot.utils`包下，主要用于生成和解析 JWT（JSON Web Token），并根据 JWT 获取当前登录用户的信息。

​	`TokenUtils`类提供了生成 JWT 和根据 JWT 获取当前登录用户信息的功能，方便在 Spring Boot 应用中进行用户身份验证和授权。



#### 61.com.tps.springbot-utils-SpringbootApplication

```
package com.tps.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }

}
```

​	这段 Java 代码是一个典型的 Spring Boot 应用的启动类。

​	`SpringbootApplication`类是 Spring Boot 应用的启动入口，借助`@SpringBootApplication`注解实现自动配置，利用`@EnableAsync`注解开启异步方法执行功能，通过`main`方法启动整个 Spring Boot 应用。



#### 62.

​	这段代码是一个 Spring Boot 项目中的用户控制器类`UserController`，用于处理与用户相关的各种 HTTP 请求，涵盖了用户的登录、注册、信息管理（增删改查）、密码修改、数据导出导入等功能。

​	`UserController`类通过各种请求处理方法，实现了对用户信息的全面管理和操作，为前端提供了丰富的 API 接口

```
package com.tps.springboot.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.User;
import com.tps.springboot.service.IRoleService;
import com.tps.springboot.service.IUserService;
import com.tps.springboot.controller.dto.UserDTO;
import com.tps.springboot.controller.dto.UserPasswordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;



@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${files.upload.path}")
    private String filesUploadPath;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;
    @Resource
    private IRoleService roleService;
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        UserDTO dto = userService.login(userDTO);
        stringRedisTemplate.opsForValue().set("userId", String.valueOf(dto.getId()));
        return Result.success(dto);
    }
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return Result.error(Constants.CODE_400, "参数错误");
        }
        return Result.success(userService.register(userDTO));
    }
    // 新增或者更新
    @PostMapping("/saveUpdateUser")
    public Result saveUpdateUser(@RequestBody User user) {
        userService.saveUpdateUser(user);
        return Result.success();
    }


    /**
     * 修改密码
     * @param userPasswordDTO
     * @return
     */
    @PostMapping("/password")
    public Result password(@RequestBody UserPasswordDTO userPasswordDTO) {
        userPasswordDTO.setPassword(SecureUtil.md5(userPasswordDTO.getPassword()));
        userPasswordDTO.setNewPassword(SecureUtil.md5(userPasswordDTO.getNewPassword()));
        userService.updatePassword(userPasswordDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(userService.removeByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    @GetMapping("/role/{role}")
    public Result findUsersByRole(@PathVariable String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role);
        List<User> list = userService.list(queryWrapper);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result findByUsername(@PathVariable String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return Result.success(userService.getOne(queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam(defaultValue = "") String username,
                               @RequestParam(defaultValue = "") String email,
                               @RequestParam(defaultValue = "") String address) {

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderByDesc("id");
//        if (!"".equals(username)) {
//            queryWrapper.like("username", username);
//        }
//        if (!"".equals(email)) {
//            queryWrapper.like("email", email);
//        }
//        if (!"".equals(address)) {
//            queryWrapper.like("address", address);
//        }
        return Result.success(userService.findPage(new Page<>(pageNum, pageSize), username, email, address));
    }


    @GetMapping("/getUserName")
    public Result getUserName() {
        return Result.success(userService.list());
    }
    @GetMapping("/totle")
    public Result totle() {
        List<User> list = userService.list();
        for (User user : list) {
            Date createTime = user.getCreateTime();

        }
        return Result.success(list.size());
    }
    /**
     * 导出接口
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<User> list = userService.list();
        // 通过工具类创建writer 写出到磁盘路径
//        ExcelWriter writer = ExcelUtil.getWriter(filesUploadPath + "/用户信息.xlsx");
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("address", "地址");
        writer.addHeaderAlias("createTime", "创建时间");
        writer.addHeaderAlias("avatarUrl", "头像");

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }
    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 方式1：(推荐) 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
//        List<User> list = reader.readAll(User.class);
        // 方式2：忽略表头的中文，直接读取表的内容
        List<List<Object>> list = reader.read(1);
        List<User> users = CollUtil.newArrayList();
        for (List<Object> row : list) {
            User user = new User();
            user.setUsername(row.get(0).toString());
            user.setPassword(row.get(1).toString());
            user.setNickname(row.get(2).toString());
            user.setEmail(row.get(3).toString());
            user.setPhone(row.get(4).toString());
            user.setAddress(row.get(5).toString());
            user.setAvatarUrl(row.get(6).toString());
            users.add(user);
        }

        userService.saveBatch(users);
        return Result.success(true);
    }

}
```