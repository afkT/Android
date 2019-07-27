import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * detail: 获取 Gradle 文件夹下随机名字
 * Created by Ttt
 * ======
 * C:\Users\Administrator\.gradle\wrapper\dists\gradle-4.1-all\bzyivzo6n839fup2jbap0tjew
 *
 * => bzyivzo6n839fup2jbap0tjew
 */
class GenerateGradleFileName {

    public static String getFileName(String distributionUrl) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(distributionUrl.getBytes());
            String str = new BigInteger(1, messageDigest.digest()).toString(36);
            return str;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String data = getFileName("https://services.gradle.org/distributions/gradle-4.1-all.zip");
        System.out.println(data); // bzyivzo6n839fup2jbap0tjew
    }

//    https://www.cnblogs.com/rainboy2010/p/7062279.html

//    那这个目录名55gk2rcmfc6p2dg9u9ohc3hw9是根据什么规则生成的呢？看似没有规律，研究了一下，发现也是按照一定规则生成的，如下：
//
//    1.从gradle/wrapper/gradle-wrapper.properties中得到distributionUrl，即https://services.gradle.org/distributions/gradle-3.3-all.zip，注意文件中的\不算
//
//    2.对distributionUrl进行MD5运算
//
//    3.根据MD5值构造一个uint 128位整数
//
//    4.将整数利用base36得到base36的值（取小写）
}
