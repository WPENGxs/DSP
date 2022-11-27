package com.wpeng.dsp.FFT;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class FourierTransformer {

    /**
     * 生成频率域图像
     * @param srcImage
     * @return
     */
    public Bitmap convert(Bitmap srcImage) {
        int iw = srcImage.getWidth();
        int ih = srcImage.getHeight();
        int[] pixels = new int[iw * ih];
        int[] newPixels;
        srcImage.getPixels(pixels,0,iw,0,0,iw,ih);
        //srcImage.getRGB(0, 0, iw, ih, pixels, 0, iw);

        // 赋初值
        int w = 1;
        int h = 1;
        // 计算进行付立叶变换的宽度和高度（2的整数次方）
        while (w * 2 <= iw) {
            w *= 2;
        }
        while (h * 2 <= ih) {
            h *= 2;
        }
        // 分配内存
        // 从左往右，从上往下排序
        Complex[] src = new Complex[h * w];
        // 从左往右，从上往下排序
        Complex[] dest = new Complex[h * w];
        newPixels = new int[h * w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 初始化src,dest
                dest[i * w + j] = new Complex();
                // 获取蓝色通道分量，也就是像素点的灰度值
                src[i * w + j] = new Complex(pixels[i * iw + j] & 0xff, 0);
            }
        }

        // 在y方向上进行快速傅立叶变换
        //
        // 一行一行的处理
        // 处理完后第一列都是各自行里面的最大值
        //
        for (int i = 0; i < h; i++) {
            Complex[] temp = new Complex[w];
            for (int k = 0; k < w; k++) {
                temp[k] = src[i * w + k];
            }
            temp = FFT.fft(temp);

            for (int k = 0; k < w; k++) {
                dest[i * w + k] = temp[k];
            }
        }

        // 对x方向进行傅立叶变换
        // 一列一列的处理，从左往右
        // 处理完后第一行都是各自列里面的最大值
        for (int i = 0; i < w; i++) {
            Complex[] temp = new Complex[h];
            for (int k = 0; k < h; k++) {
                temp[k] = dest[k * w + i];
            }
            temp = FFT.fft(temp);

            for (int k = 0; k < h; k++) {
                dest[k * w + i] = temp[k];
            }
        }

        // 打印傅里叶频率域图像
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 从左到右,从上到下遍历
                double re = dest[i * w + j].re;
                double im = dest[i * w + j].im;

                int ii = 0, jj = 0;
                // 缩小数值，方便展示
                int temp = (int) (Math.sqrt(re * re + im * im) / 100);

                // 用十字将图像切割为4等分，然后每个部分旋转180度，再拼接起来，方便观察
                if (i < h / 2) {
                    ii = i + h / 2;
                } else {
                    ii = i - h / 2;
                }
                if (j < w / 2) {
                    jj = j + w / 2;
                } else {
                    jj = j - w / 2;
                }
                newPixels[ii * w + jj] = (clamp(temp) << 16) | (clamp(temp) << 8) | clamp(temp);;
            }
        }

        Bitmap destImg = Bitmap.createBitmap(iw,ih, Bitmap.Config.RGB_565);
        destImg.setPixels(newPixels,0,w,0,0,w,h);
        //destImg.setRGB(0, 0, w, h, newPixels, 0, w);

        return destImg;
    }

    /**
     * 从频率还原为图像
     * 已经实现了通过去除低频率提取边缘和通过去除高频率模糊图像两个功能
     * @param srcImage
     * @return
     */
    public Bitmap recover(Bitmap srcImage,boolean isLowPass) {
        int iw = srcImage.getWidth();
        int ih = srcImage.getHeight();
        int[] pixels = new int[iw * ih];
        int[] newPixels;
        srcImage.getPixels(pixels,0,iw,0,0,iw,ih);
        //srcImage.getRGB(0, 0, iw, ih, pixels, 0, iw);

        // 赋初值
        int w = 1;
        int h = 1;
        // 计算进傅里叶变换的宽度和高度（2的整数次方）
        while (w * 2 <= iw) {
            w *= 2;
        }
        while (h * 2 <= ih) {
            h *= 2;
        }
        // 分配内存
        // 从左往右，从上往下排序
        Complex[] src = new Complex[h * w];
        // 从左往右，从上往下排序
        Complex[] dest = new Complex[h * w];
        newPixels = new int[h * w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 初始化src,dest
                dest[i * w + j] = new Complex();
                // 获取蓝色通道分量，也就是像素点的灰度值
                src[i * w + j] = new Complex(pixels[i * iw + j] & 0xff, 0);
            }
        }

        // 在y方向上进行快速傅立叶变换
        //
        // 先一行一行的处理
        // 处理完后第一列都是各自行里面的最大值
        //
        for (int i = 0; i < h; i++) {
            Complex[] temp = new Complex[w];
            for (int k = 0; k < w; k++) {
                temp[k] = src[i * w + k];
            }
            temp = FFT.fft(temp);

            for (int k = 0; k < w; k++) {
                dest[i * w + k] = temp[k];
            }
        }

        // 对x方向进行傅立叶变换
        // 再一列一列的处理，从左往右
        // 处理完后第一行都是各自列里面的最大值
        for (int i = 0; i < w; i++) {
            Complex[] temp = new Complex[h];
            for (int k = 0; k < h; k++) {
                temp[k] = dest[k * w + i];
            }
            temp = FFT.fft(temp);

            for (int k = 0; k < h; k++) {
                dest[k * w + i] = temp[k];
            }
        }

        // 去除指定频率部分，然后还原为图像
        int halfWidth = w/2;
        int halfHeight = h/2;
        // 比例越小，边界越突出
        double res = 0.2;

        // 比例越大，图片越模糊
        double del = 0.3;
        // 先对列进行逆傅里叶变换，并去除指定频率。和傅里叶变换顺序相反
        for (int i = 0; i < w; i++) {
            Complex[] temp = new Complex[h];
            if(isLowPass){
                for (int k = 0; k < h; k++) {
                    // 去除低频率，保留边缘，因为频率图尚未做十字切割并分别旋转180度，所以此时图像dest[] 的四个角落是低频率，而中心部分是高频率
                    // 第一个点(0,0)不能删除，删掉后整张图片就变黑了
                    if (i == 0 && k == 0) {
                        temp[k] = dest[k * w + i];
                    } else if ((i <res*halfWidth && k < res*halfHeight) || (i <res*halfWidth && k > (1-res)*halfHeight)
                            || (i > (1-res)*halfWidth && k <res*halfHeight) || (i > (1-res)*halfWidth && k > (1-res)*halfHeight)) {
                        temp[k] = new Complex(0, 0);
                    } else {
                        temp[k] = dest[k * w + i];
                    }

                    // 去除高频率，模糊图像。和去除低频率代码冲突，需要先屏蔽上面去除低频率的代码才能执行
				/*if ((i > ((1-del)*halfWidth) && k > ((1-del)*halfHeight)) && (i <((1+del)*halfWidth) && k < ((1+del)*halfHeight))) {
					temp[k] = new Complex(0, 0);
				} else {
					temp[k] = dest[k * w + i];
				}*/
                }
            }else {
                for (int k = 0; k < h; k++) {
                    // 去除高频率，模糊图像。
				if ((i > ((1-del)*halfWidth) && k > ((1-del)*halfHeight)) && (i <((1+del)*halfWidth) && k < ((1+del)*halfHeight))) {
					temp[k] = new Complex(0, 0);
				} else {
					temp[k] = dest[k * w + i];
				}
                }
            }

            temp = FFT.ifft(temp);
            for (int k = 0; k < h; k++) {
                // 这边需要除以N，也就是temp[]的长度
                dest[k * w + i].im = temp[k].im / h;
                dest[k * w + i].re = temp[k].re / h;
            }
        }

        // 再对行进行逆傅里叶变换
        for (int i = 0; i < h; i++) {
            Complex[] temp = new Complex[w];
            for (int k = 0; k < w; k++) {
                temp[k] = dest[i * w + k];
            }
            temp = FFT.ifft(temp);
            for (int k = 0; k < w; k++) {
                // 这边需要除以N，也就是temp[]的长度
                dest[i * w + k].im = temp[k].im / w;
                dest[i * w + k].re = temp[k].re / w;
            }
        }

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                // 从左到右,从上到下遍历
                double re = dest[i * w + j].re;
                int temp = (int) re;
                newPixels[i * w + j] = (clamp(temp) << 16) | (clamp(temp) << 8) | clamp(temp);;
            }
        }

        //Bitmap destImg = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        //destImg.setRGB(0, 0, w, h, newPixels, 0, w);
        Bitmap destImg = Bitmap.createBitmap(iw,ih, Bitmap.Config.RGB_565);
        destImg.setPixels(newPixels,0,w,0,0,w,h);

        return destImg;
    }

    /**
     * 如果像素点的值超过了0-255的范围,予以调整
     *
     * @param value 输入值
     * @return 输出值
     */
    private int clamp(int value) {
        return value > 255 ? 255 : (Math.max(value, 0));
    }
}

