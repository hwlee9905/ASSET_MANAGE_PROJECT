package com.mcnc.bizmob.view.image;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * 01.클래스 설명 : 이미지 관련 작업을 수행하는 Util class. <br>
 * 02.제품구분 : bizMob 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 이미지 회전 thumbnail image 생성..<br>
 * 04.관련 API/화면/서비스 : Logger, imageViewerActivity <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-26                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class ImageViewUtil {

    /**
     * 이미지 파일 uri로 부터 rotation 값를 반환하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-26                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param uri 이미지 파일 uri.
     */
    public static int exifRotationDegrees(String uri) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(uri);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            }
        } catch (IOException e) {
        }

        return 0;
    }

    /**
     * Bitmap 이미지를 해당 수치 값으로 rotate하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-26                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param degrees rotate 할 degree 값. (0~360)
     * @param bitmap  Bitmap 객체
     * @return rotate 된 Bitmap 객체.
     */
    public static Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try {
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
            } catch (OutOfMemoryError ex) {

            }
        }
        return bitmap;
    }

    /**
     * 이미지 파일의 Thumbnail 이미지를 반환하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-26                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param cr                ContentResolver 객체.
     * @param imageId           이미지 파일의 고유 id.
     * @param orientationDegree orientation degree 값.
     * @param sampleSize        Thumbnail image 사이즈 값.
     * @return Thumbnail 이미지.
     */
    public static Bitmap getImageThumbnailBitmap(ContentResolver cr, int imageId,
                                                 int orientationDegree, int sampleSize) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(cr,
                    imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String uri = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                bitmap = BitmapFactory.decodeFile(uri, options);
            }
            cursor.close();
            if (bitmap == null) {
                // this function will always get thumbnail, but it is slow
                // so use it only if the above function can't get thumbnail
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, imageId,
                        MediaStore.Images.Thumbnails.MINI_KIND, options);
            }
            bitmap = rotate(bitmap, orientationDegree);
        } catch (Exception e) {

        }
        return bitmap;
    }

    /**
     * 동영상 파일의 Thumbnail 이미지를 반환하는 메소드. <br>
     * <p>
     * <pre>
     * 수정이력 <br>
     * **********************************************************************************************************************************<br>
     *  수정일                               이름                               변경내용<br>
     * **********************************************************************************************************************************<br>
     *  2016-09-26                           박재민                             최초 작성<br>
     *
     * </pre>
     *
     * @param cr                ContentResolver 객체.
     * @param imageId           이미지 파일의 고유 id.
     * @param orientationDegree orientation degree 값.
     * @param sampleSize        Thumbnail image 사이즈 값.
     * @return Thumbnail 이미지.
     */
    public static Bitmap getVideoThumbnailBitmap(ContentResolver cr, int imageId,
                                                 int orientationDegree, int sampleSize) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, imageId, MediaStore.Video.Thumbnails.MINI_KIND, null);
            if (bitmap == null) {
                // this function will always get thumbnail, but it is slow
                // so use it only if the above function can't get thumbnail
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, imageId, MediaStore.Video.Thumbnails.MINI_KIND, options);
            }
            bitmap = rotate(bitmap, orientationDegree);
        } catch (Exception e) {

        }
        return bitmap;
    }

}
