package com.dying.usercenter.common;

public class ResultUtils<T> {
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0 , data , "ok");
    }
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse(errorCode);
    }
    public static BaseResponse error(int Code , String message , String description){
        return new BaseResponse(Code ,null, message ,description);
    }
    public static BaseResponse error(ErrorCode errorCode , String description){
        return new BaseResponse(errorCode.getCode() , null ,errorCode.getMessage() , description);
    }

    public static BaseResponse error(ErrorCode errorCode , String message , String description){
        return new BaseResponse(errorCode.getCode() ,null, message ,description);
    }
}
