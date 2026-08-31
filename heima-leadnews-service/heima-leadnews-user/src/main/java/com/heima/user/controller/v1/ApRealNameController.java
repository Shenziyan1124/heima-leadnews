package com.heima.user.controller.v1;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.RealNameListDto;
import com.heima.user.service.ApRealNameService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Api(tags = "用户实名认证")
@RequiredArgsConstructor
public class ApRealNameController {

    private final ApRealNameService apRealNameService;

    @PostMapping("/list")
    @ApiOperation("用户实名认证列表")
    public ResponseResult realNameList(@RequestBody RealNameListDto dto) {
        return apRealNameService.realNameList(dto);
    }

    @PostMapping("/authPass")
    @ApiOperation("用户实名认证通过")
    public ResponseResult authPass(@RequestBody RealNameListDto dto) {
        return apRealNameService.authPass(dto);
    }

    @PostMapping("/authFail")
    @ApiOperation("用户实名认证失败")
    public ResponseResult authFail(@RequestBody RealNameListDto dto) {
        return apRealNameService.authFail(dto);
    }
}
