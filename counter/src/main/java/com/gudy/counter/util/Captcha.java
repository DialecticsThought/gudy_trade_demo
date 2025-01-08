package com.gudy.counter.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * @Description
 * @Author veritas
 * @Data 2025/1/5 18:43
 */
public class Captcha {
    /*
     * 验证码
     * */
    private String code;

    /*
     * 图片
     * */
    private BufferedImage bufferedImage;
    /*
     * 随机数生成器
     * */
    private Random random = new Random();

    public Captcha(int width, int height, int codeCount, int lineCount) {
        // 生成图像
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 背景色
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(getRandomColor(200, 250));
        graphics.fillRect(0, 0, width, height);
        Font font = new Font("Fixedsys", Font.BOLD, height - 5);
        // 生成干扰线 噪点 需要知道每条线段的起始点和终止点
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(height);
            graphics.setColor(getRandomColor(200, 250));
            graphics.drawLine(xs, ys, xe, ye);
        }
        float yawpRate = 0.01f;
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            bufferedImage.setRGB(x, y, random.nextInt(255));
        }
        // 添加字符
        this.code = randomStr(codeCount);
        int fontWidth = width / codeCount;
        int fontHeight = height - 5;
        for (int i = 0; i < codeCount; i++) {
            // 取出第i个字符
            String str = this.code.substring(i, i + 1);
            // 设置随机的颜色
            graphics.setColor(getRandomColor(1, 255));
            // 画画 传入 字符 和对应的坐标
            graphics.drawString(str, i * fontWidth + 3, fontHeight - 3);
        }


    }

    /*
     * 随机生成颜色 两个入参是背景颜色的区间
     * */
    public Color getRandomColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);

        return new Color(r, g, b);
    }

    /*
     * 随机生成字符的数量
     * */
    private String randomStr(int codeCount) {
        //定义一个字典 所有的字符从字典中取
        String str = "ABCDEFGHJKMNOPQRSTUVWXYZabcdefghjkmnopqrstuvwxyz1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        int length = str.length() - 1;

        double r;
        for (int i = 0; i < codeCount; i++) {
            r = (Math.random()) * length;
            stringBuilder.append(str.charAt((int) r));
        }
        return stringBuilder.toString();

    }

    public String getCode() {
        return code.toLowerCase();
    }


    public String getBase64ByteStr() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 用工具类实现写入
        try {
            ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
            // 用工具类实现编码
            String s = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

            // 把空格删去
            s = s.replaceAll("\n","")
                    .replaceAll("\r","");
            // TODO 这里 不是 “,” 是“;”
            return "data:image/jpg;base64," + s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
