package dp;

import dp.utils.DimenItem;
import dp.utils.Tools;
import dp.utils.XmlIO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @Author: duke
 * @DateTime: 2016-08-24 16:16
 * @UpdateTime: 2017-09-29 09:55
 * @Description: 入口
 */
final class GenerateDPValueFiles {

    // ==================
    // ==== 调用方法 ====
    // ==================

    public static void main(String[] args) {

        // 基准文件, 在本项目目录下 res\values\dimens.xml

        // 只需要把想要适配的文件替换 dimens.xml, 并且修改基准dp baseDP => 以常用开发测试手机的dp为基准

        // 获取当前目录的绝对路径
        String resFolderPath = new File(System.getProperty("user.dir") + "/res/").getAbsolutePath();

        // 生成文件
        new GenerateDPValueFiles().start(true, resFolderPath, true);
    }

    // ========================================
    // ========================================
    // ========================================

    // 基准dp，比喻：360dp
    private double baseDP = 360;

    // 生成的values目录格式(代码中替换XXX字符串)
    private String LETTER_REPLACE = "XXX";

    // values-w410dp，这个目录需要删除
    private String VALUES_OLD_FOLDER = "values-wXXXdp";

    // values-sw410dp
    private String VALUES_NEW_FOLDER = "values-swXXXdp";

    // 是否删除旧的目录格式
    private final boolean isDeleteAnotherFolder = true;

    // 去重复的数据集合
    private HashSet<Double> dataSet = new HashSet<>();

    // 对应的dp值
    private static final ArrayList<String> listDPs = new ArrayList<>();

    static {
        // http://screensiz.es/
        listDPs.add("384");
        listDPs.add("392");
        listDPs.add("400");
        listDPs.add("410");
        listDPs.add("411");
        listDPs.add("480");
        listDPs.add("533");
        listDPs.add("592");
        listDPs.add("600");
        listDPs.add("640");
        listDPs.add("662");
        listDPs.add("720");
        listDPs.add("768");
        listDPs.add("800");
        listDPs.add("811");
        listDPs.add("820");
        listDPs.add("960");
        listDPs.add("961");
        listDPs.add("1024");
        listDPs.add("1280");
        listDPs.add("1365");
    }

    /**
     * 适配文件调用入口
     * @param isFontMatch 字体是否也适配(是否与dp尺寸一样等比缩放)
     * @param resFolderPath base dimens.xml 文件的res目录
     * @param isUseNewFolder 是否创建 values-swXXXdp 新格式的目录
     * @return 返回消息
     */
    public void start(boolean isFontMatch, String resFolderPath, boolean isUseNewFolder) {
        // 添加默认的数据
        for (String aDefaultDPArr : listDPs) {
            if (aDefaultDPArr == null || "".equals(aDefaultDPArr.trim())) {
                continue;
            }
            try {
                dataSet.add(Double.parseDouble(aDefaultDPArr.trim()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        System.out.println("基准宽度dp值：[ " + Tools.cutLastZero(baseDP) + " dp ]");
        System.out.println("本次待适配的宽度dp值: [ " + Tools.getOrderedString(dataSet) + " ]");
        // 获取基准的dimens.xml文件
        String baseDimenFilePath = resFolderPath + File.separator + "values" + File.separator + "dimens.xml";
        File testBaseDimenFile = new File(baseDimenFilePath);
        // 判断基准文件是否存在
        if (!testBaseDimenFile.exists()) {
            System.out.println("DK WARNING:  \"./res/values/dimens.xml\" 路径下的文件找不到!");
            return;
        }
        System.out.println("文件路径: " + baseDimenFilePath);
        // 解析源dimens.xml文件
        ArrayList<DimenItem> list = XmlIO.readDimenFile(baseDimenFilePath);
        if (list == null || list.size() <= 0) {
            System.out.println("DK WARNING:  \"./res/values/dimens.xml\" 文件无数据!");
            return;
        } else {
            System.out.println("OK \"./res/values/dimens.xml\" 基准dimens文件解析成功!");
        }
        try {
            // 循环指定的dp参数，生成对应的dimens-swXXXdp.xml文件
            Iterator<Double> iterator = dataSet.iterator();
            while (iterator.hasNext()) {
                double item = iterator.next();
                // 获取当前dp除以baseDP后的倍数
                double multiple = item / baseDP;

                // 待输出的目录
                String outFolderPath = "";
                // 待删除的目录
                String delFolderPath = "";
                // values目录上带的dp整数值
                String folderDP = String.valueOf((int) item);

                if (isUseNewFolder) {
                    outFolderPath = VALUES_NEW_FOLDER.replace(LETTER_REPLACE, folderDP);
                    delFolderPath = VALUES_OLD_FOLDER.replace(LETTER_REPLACE, folderDP);
                } else {
                    outFolderPath = VALUES_OLD_FOLDER.replace(LETTER_REPLACE, folderDP);
                    delFolderPath = VALUES_NEW_FOLDER.replace(LETTER_REPLACE, folderDP);
                }
                outFolderPath = resFolderPath + File.separator + outFolderPath + File.separator;
                delFolderPath = resFolderPath + File.separator + delFolderPath + File.separator;

                if (isDeleteAnotherFolder) {
                    // 删除以前适配方式的目录values-wXXXdp
                    File oldFile = new File(delFolderPath);
                    if (oldFile.exists() && oldFile.isDirectory() && Tools.isOldFolder(oldFile.getName(), isUseNewFolder)) {
                        // 找出res目录下符合要求的values目录，然后递归删除values目录
                        Tools.deleteFile(oldFile);
                    }
                }

                // 生成新的目录values-swXXXdp
                new File(outFolderPath).mkdirs(); // 创建当前dp对应的dimens文件目录

                // 生成的dimens文件的路径
                String outPutFile = outFolderPath + "dimens.xml";
                // 生成目标文件dimens.xml输出目录
                XmlIO.createDestinationDimens(isFontMatch, list, multiple, outPutFile);
            }

            System.out.println("OK ALL OVER，全部生成完毕！");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}