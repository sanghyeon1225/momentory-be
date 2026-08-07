package com.momentory.memo.presentation;

import com.momentory.common.presentation.ApiErrorResponse;
import com.momentory.auth.security.Login;
import com.momentory.auth.security.LoginPrincipal;
import com.momentory.memo.application.DailyMemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "Daily Memos", description = "하루 메모 API")
@RestController
@RequestMapping("/api/v1/memos")
public class DailyMemoController {

    private final DailyMemoService dailyMemoService;

    public DailyMemoController(DailyMemoService dailyMemoService) {
        this.dailyMemoService = dailyMemoService;
    }

    @Operation(summary = "날짜별 하루 메모 조회")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "하루 메모 조회 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DailyMemoResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class), examples = @ExampleObject(
                    name = "invalidDateFormat",
                    value = """
                            {
                              "code": "INVALID_REQUEST",
                              "message": "잘못된 요청입니다."
                            }
                            """
            ))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class), examples = @ExampleObject(
                    name = "AUTHENTICATION_REQUIRED",
                    value = """
                            {
                              "code": "AUTHENTICATION_REQUIRED",
                              "message": "인증이 필요합니다."
                            }
                            """
            ))),
            @ApiResponse(responseCode = "404", description = "하루 메모를 찾을 수 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class), examples = @ExampleObject(
                    name = "DAILY_MEMO_NOT_FOUND",
                    value = """
                            {
                              "code": "DAILY_MEMO_NOT_FOUND",
                              "message": "하루 메모를 찾을 수 없습니다."
                            }
                            """
            )))
    })
    @GetMapping(value = "/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DailyMemoResponse getDailyMemo(
            @Login LoginPrincipal principal,
            @Parameter(example = "2026-08-07") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return DailyMemoResponse.from(dailyMemoService.getDailyMemo(principal.userId(), date));
    }

    @Operation(summary = "날짜별 하루 메모 저장 또는 수정")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "하루 메모 저장 또는 수정 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DailyMemoResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class), examples = {
                    @ExampleObject(
                            name = "invalidDateFormat",
                            value = """
                                    {
                                      "code": "INVALID_REQUEST",
                                      "message": "잘못된 요청입니다."
                                    }
                                    """
                    ),
                    @ExampleObject(
                            name = "malformedRequestBody",
                            value = """
                                    {
                                      "code": "INVALID_REQUEST",
                                      "message": "잘못된 요청입니다."
                                    }
                                    """
                    ),
                    @ExampleObject(
                            name = "contentRequiredOrBlank",
                            value = """
                                    {
                                      "code": "INVALID_REQUEST",
                                      "message": "메모 내용을 입력해주세요."
                                    }
                                    """
                    )
            })),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class), examples = @ExampleObject(
                    name = "AUTHENTICATION_REQUIRED",
                    value = """
                            {
                              "code": "AUTHENTICATION_REQUIRED",
                              "message": "인증이 필요합니다."
                            }
                            """
            )))
    })
    @PutMapping(value = "/{date}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DailyMemoResponse saveDailyMemo(
            @Login LoginPrincipal principal,
            @Parameter(example = "2026-08-07") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody DailyMemoRequest request
    ) {
        return DailyMemoResponse.from(dailyMemoService.saveDailyMemo(principal.userId(), date, request.content()));
    }

    @Operation(summary = "날짜별 하루 메모 삭제")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "하루 메모 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class), examples = @ExampleObject(
                    name = "invalidDateFormat",
                    value = """
                            {
                              "code": "INVALID_REQUEST",
                              "message": "잘못된 요청입니다."
                            }
                            """
            ))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class), examples = @ExampleObject(
                    name = "AUTHENTICATION_REQUIRED",
                    value = """
                            {
                              "code": "AUTHENTICATION_REQUIRED",
                              "message": "인증이 필요합니다."
                            }
                            """
            )))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{date}")
    public void deleteDailyMemo(
            @Login LoginPrincipal principal,
            @Parameter(example = "2026-08-07") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        dailyMemoService.deleteDailyMemo(principal.userId(), date);
    }
}
