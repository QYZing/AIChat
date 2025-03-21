package com.dying.usercenter.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoSize;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Objects;

/**
 * ffmpeg工具类
 *
 * @since 2024/1/22
 */
@Slf4j
public class FfmpegUtil {

    public static void main(String[] args) throws Exception {
        VideoSize videoSize = new VideoSize(480, 800);
        System.out.println(GetCompressSize(videoSize, 720, 480));
    }

    public static String convertAudio(MultipartFile file) {
        if (file.isEmpty()) {
            return "";
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||!originalFilename.toLowerCase().endsWith(".webm")) {
            System.out.println("输入文件不是 WebM 格式"+ HttpStatus.BAD_REQUEST);
        }

        try {
            // 创建临时文件
            Path tempFilePath = Files.createTempFile("audio-", ".webm");
            Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);

            // 构建输出文件路径
            String outputFilePath = tempFilePath.toString().replace(".webm", ".pcm");

            // 执行转换
            if (convertWebmToPcm(tempFilePath.toString(), outputFilePath)) {
                return outputFilePath;
            } else {
                System.out.println("转换失败"+ HttpStatus.INTERNAL_SERVER_ERROR);
                return "";
            }
        } catch (IOException e) {
            return "";
        }
    }

    private static boolean convertWebmToPcm(String inputPath, String outputPath) {
        ProcessWrapper ffmpeg = null;
        try {
            ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            ffmpeg.addArgument("-y");
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(inputPath);
            ffmpeg.addArgument("-acodec");
            ffmpeg.addArgument("pcm_s16le");

            ffmpeg.addArgument("-f");
            ffmpeg.addArgument("s16le");
            ffmpeg.addArgument("-ac");
            ffmpeg.addArgument("1");
            ffmpeg.addArgument("-ar");
            ffmpeg.addArgument("16000");
            ffmpeg.addArgument(outputPath);
            ffmpeg.execute();

            //等待完成
            String errorMsg = WaitFfmpegFinish(ffmpeg);
            if (StringUtils.hasLength(errorMsg)) {
                log.error("转音频,文件:" + inputPath, errorMsg);
                return false;
            }
            return true;
        } catch (IOException | InterruptedException e) {
            System.err.println("执行 FFmpeg 命令时出现错误: " + e.getMessage());
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过本地路径获取多媒体文件信息(宽，高，时长，编码等)
     *
     * @param filePath 文件路径
     * @return MultimediaInfo 媒体对象,包含 (宽，高，时长，编码等)
     */
    public static MultimediaInfo GetMediaInfo(String filePath) {
        try {
            return new MultimediaObject(new File(filePath)).getInfo();
        } catch (EncoderException e) {
            log.error("获取媒体信息异常!", e);
        }
        return null;
    }

    /**
     * 修改视频分辨率
     * 修改分辨率超过原视频,不能提高画质
     * 标清SD（Standard Definition） 480p 640x480 720x480
     * 高清 HD（High Definition） 720p 960x720 1280x720
     * 1080p 1440x1080 1920x1080
     * 超高清UHD（Ultra High Definition） 4k 4096×3112 4096*2160
     *
     * @param inputPath  视频来源地址
     * @param outputPath 输出视频地址
     * @param width      宽度
     * @param height     高度
     */
    public static boolean ChangeScale(String inputPath, String outputPath, int width, int height) {
        ProcessWrapper ffmpeg = null;
        try {
            if (new File(outputPath).exists()) {
                log.error("目标文件已存在,outputPath:{}", outputPath);
                return false;
            }

            ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(inputPath);
            ffmpeg.addArgument("-vf");
            ffmpeg.addArgument("scale=w=" + width + ":h=" + height);
            ffmpeg.addArgument("-c:a");
            ffmpeg.addArgument("copy");
            ffmpeg.addArgument(outputPath);
            ffmpeg.execute();

            //等待完成
            String errorMsg = WaitFfmpegFinish(ffmpeg);
            if (StringUtils.hasLength(errorMsg)) {
                log.error("修改视频分辨率失败,文件:" + inputPath, errorMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("修改视频分辨率异常", e);
        } finally {
            if (ffmpeg != null) {
                ffmpeg.destroy();
            }
        }
        return false;
    }

    /**
     * 调整视频质量
     *
     * @param inputPath    输入视频
     * @param outputPath   输出视频
     * @param qualityLevel 质量等级,0-51,0最高
     */
    public static boolean ChangeQuality(String inputPath, String outputPath, int qualityLevel) {
        ProcessWrapper ffmpeg = null;
        try {
            if (new File(outputPath).exists()) {
                log.error("目标文件已存在,outputPath:{}", outputPath);
                return false;
            }

            ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(inputPath);
            ffmpeg.addArgument("-q:v");
            ffmpeg.addArgument(qualityLevel + "");
            ffmpeg.addArgument("-c:a");
            ffmpeg.addArgument("copy");
            ffmpeg.addArgument(outputPath);
            ffmpeg.execute();

            //等待完成
            String errorMsg = WaitFfmpegFinish(ffmpeg);
            if (StringUtils.hasLength(errorMsg)) {
                log.error("修改视频视频质量失败,文件:" + inputPath, errorMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("调整视频质量异常", e);
        } finally {
            if (ffmpeg != null) {
                ffmpeg.destroy();
            }
        }
        return false;
    }

    /**
     * 复制视频片段
     *
     * @param inputPath  视频来源地址
     * @param outputPath 输出视频地址
     * @param startTime  开始时间,01:02:03在视频的第1小时第2分钟第3秒处
     * @param endTime    结束时间,格式同startTime
     */
    public static boolean CopyVideo(String inputPath, String outputPath, String startTime, String endTime) {
        ProcessWrapper ffmpeg = null;
        try {
            if (new File(outputPath).exists()) {
                log.error("目标文件已存在,outputPath:{}", outputPath);
                return false;
            }
            ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(inputPath);
            ffmpeg.addArgument("-ss");
            ffmpeg.addArgument(startTime);
            ffmpeg.addArgument("-to");
            ffmpeg.addArgument(endTime);
            ffmpeg.addArgument("-c:v");
            ffmpeg.addArgument("copy");
            ffmpeg.addArgument("-c:a");
            ffmpeg.addArgument("copy");
            ffmpeg.addArgument(outputPath);
            //执行
            ffmpeg.execute();

            //等待完成
            String errorMsg = WaitFfmpegFinish(ffmpeg);
            if (StringUtils.hasLength(errorMsg)) {
                log.error("复制视频失败,文件:" + inputPath, errorMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("复制视频片段异常", e);
        } finally {
            if (ffmpeg != null) {
                ffmpeg.destroy();
            }
        }
        return false;
    }

    /**
     * 格式转换
     *
     * @param inputPath  视频来源地址
     * @param outputPath 输出视频地址
     */
    public static boolean TransferFormat(String inputPath, String outputPath) {
        ProcessWrapper ffmpeg = null;
        try {
            if (new File(outputPath).exists()) {
                log.error("目标文件已存在,outputPath:{}", outputPath);
                return false;
            }
            ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(inputPath);
            ffmpeg.addArgument("-c");
            ffmpeg.addArgument("copy");
            ffmpeg.addArgument(outputPath);
            //执行
            ffmpeg.execute();

            //等待完成
            String errorMsg = WaitFfmpegFinish(ffmpeg);
            if (StringUtils.hasLength(errorMsg)) {
                log.error("转换视频格式失败,文件:" + inputPath, errorMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("转换视频格式异常", e);
        } finally {
            if (ffmpeg != null) {
                ffmpeg.destroy();
            }
        }
        return false;
    }

    /**
     * 生成视频第多少秒的缩略图
     */
    public static boolean GenerateThumbnail(String inputPath, int second, String outputPath) {
        ProcessWrapper ffmpeg = null;
        try {
            File target = new File(outputPath);
            if (target.exists()) {
                log.error("目标文件已存在,outputPath:{}", outputPath);
                return false;
            }

            ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(inputPath);
            ffmpeg.addArgument("-ss");//第多少秒的视频画面
            ffmpeg.addArgument("" + second);
            ffmpeg.addArgument("-frames:v");//第多少秒的视频画面
            ffmpeg.addArgument("1");
            ffmpeg.addArgument(outputPath);
            ffmpeg.execute();

            //等待完成
            String errorMsg = WaitFfmpegFinish(ffmpeg);
            if (StringUtils.hasLength(errorMsg)) {
                log.error("获取缩略图失败,文件:" + inputPath, errorMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("获取视频缩略图异常", e);
        } finally {
            if (ffmpeg != null) {
                ffmpeg.destroy();
            }
        }
        return false;
    }

    /**
     * 添加水印
     *
     * @param inputPath     输入视频路径
     * @param waterMarkPath 水印文件路径
     * @param x             距离左上角水平距离
     * @param y             距离左上角垂直距离
     * @param outputPath    输出视频路径
     */
    public static boolean AddWatermark(String inputPath, String waterMarkPath,
                                       int x, int y, String outputPath) {
        ProcessWrapper ffmpeg = null;
        try {
            File target = new File(outputPath);
            if (target.exists()) {
                log.error("目标文件已存在,outputPath:{}", outputPath);
                return false;
            }

            ffmpeg = new DefaultFFMPEGLocator().createExecutor();
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(inputPath);
            ffmpeg.addArgument("-i");
            ffmpeg.addArgument(waterMarkPath);
            ffmpeg.addArgument("-filter_complex");
            ffmpeg.addArgument("overlay=" + x + ":" + y);
            ffmpeg.addArgument(outputPath);
            ffmpeg.execute();

            //等待完成
            String errorMsg = WaitFfmpegFinish(ffmpeg);
            if (StringUtils.hasLength(errorMsg)) {
                log.error("视频添加水印失败,文件:" + inputPath, errorMsg);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("视频添加水印异常", e);
            return false;
        } finally {
            if (ffmpeg != null) {
                ffmpeg.destroy();
            }
        }
    }

    /**
     * 获取ffmpeg默认路径
     */
    public static String GetDefaultFFMPEGPath() {
        return new File(System.getProperty("java.io.tmpdir"), "jave/").getAbsolutePath();
    }

    /**
     * 将第多少秒转换为视频时间(HH:mm:ss)
     * 例: 第70秒为00:01:10
     *
     * @param second 视频中的第多少秒
     */
//    public static String TransferVideoTime(int second) {
//        var calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.add(Calendar.SECOND, second);
//        return DateUnitl.ToString(calendar.getTime(), "HH:mm:ss");
//    }

    /**
     * 等待命令执行完成
     *
     * @return 成功返回null, 否则返回错误信息
     */
    private static String WaitFfmpegFinish(ProcessWrapper processWrapper) throws Exception {
        //存储执行错误信息
        var errorSb = new StringBuilder();
        //ffmpeg的执行过程打印在errorStream中
        try (BufferedReader br = new BufferedReader(new InputStreamReader(processWrapper.getErrorStream()))) {
            String processInfo;
            while ((processInfo = br.readLine()) != null) {
                errorSb.append(processInfo).append("\r\n");
            }
            //执行完成后,获取处理退出编码
            int processExitCode = processWrapper.getProcessExitCode();
            if (processExitCode != 0) {//非0执行失败,返回执行过程信息
                return errorSb.toString();
            }
        } catch (Exception e) {
            log.error("WaitFfmpegFinish异常!", e);
            return errorSb.toString();
        }
        return null;
    }

    /**
     * 获取按照指定宽、高比压缩的视频大小
     * 宽度和高度必须是2的倍数
     */
    public static VideoSize GetCompressSize(VideoSize oldVideoSize, int weight, int height) {
        assert oldVideoSize != null;
        int oldWidth = oldVideoSize.getWidth();
        int oldHeight = oldVideoSize.getHeight();
        double ratio = (double) oldWidth / oldHeight;
        if (ratio > 1.0) {//宽屏视频,根据高度缩小
            if (oldHeight <= height) {//原高度比预期小,直接返回
                return oldVideoSize;
            }
            int newWeight = (oldWidth * height) / oldHeight;
            if (newWeight % 2 != 0) {
                newWeight++;
            }
            return new VideoSize(newWeight, height);
        } else {//窄屏视频,根据宽度缩小
            if (oldWidth <= weight) {//原宽带度比预期小,直接返回
                return oldVideoSize;
            }
            int newHeight = (oldHeight * weight) / oldWidth;
            if (newHeight % 2 != 0) {
                newHeight++;
            }
            return new VideoSize(weight, newHeight);
        }
    }

//    //判断视频大小是否相等
//    public static boolean Equal(VideoSize videoSize1, VideoSize videoSize2) {
//        if (videoSize1 == null &amp;&amp; videoSize2 == null) {
//            return true;
//        } else if (videoSize1 == null || videoSize2 == null) {
//            return false;
//        } else {
//            return Objects.equals(videoSize1.getHeight(), videoSize2.getHeight())
//                    &amp;&amp; Objects.equals(videoSize1.getWidth(), videoSize2.getWidth());
//        }
//    }

}
