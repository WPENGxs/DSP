clear;

origin = imread('origin.jpg');

% 使用离散傅里叶变换压缩

[z, k] = fft_compress(origin, 0.8);
subplot(1, 2, 1);
imshow(z);
subplot(1, 2, 2);
imshow(k), title('FFT 比例 0.8');