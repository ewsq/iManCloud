package per.zdy.iManCloud.web;


import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import per.zdy.iManCloud.domain.pojo.User;
import per.zdy.iManCloud.domain.pojo.UserEntity;
import per.zdy.iManCloud.service.LoginService;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

import static per.zdy.iManCloud.share.PublicValue.*;

@Controller
@ResponseBody
public class AdminController {

    @Autowired
    LoginService loginService;

    @GetMapping(value = "/test")
    public ModelAndView test(HttpServletRequest req) {
        // UserEntity userEntity = getCurrentUser(req);
        UserEntity user = new UserEntity();
        user.setLoginName("tom");
        user.setId("234");
        user.setBindType("1");
        ModelAndView mv = new ModelAndView();
        mv.addObject("user", user);
        mv.setViewName("/model/Upload.html");
        return mv;
    }

    private final int BYTES_PER_SLICE = 1<<20;
    @RequestMapping(value="sliceUpload",method= RequestMethod.POST)
    public int upload(@RequestParam("slice") MultipartFile slice, String fileName, int index) {
        int result = 0;
        if(slice.isEmpty()){
            return 0;
        }
        int size = (int) slice.getSize();
        System.out.println(fileName + "-->" + size);

        String path = "C:/迅雷下载" ;
        File dest = new File(path + "/" + fileName);
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(dest,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //判断文件父目录是否存在
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }
        try {
            //保存文件
            byte[] bytes = slice.getBytes();
                randomAccessFile.seek(index*BYTES_PER_SLICE);
            randomAccessFile.write(bytes);
            result = 1;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static String FILENAME = "";

    @Value("${xdja.upload.file.path.temp}")
    private String decryptFilePathTemp;

    @GetMapping("/webuploader")
    public ModelAndView webuploader() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/model/webupload.html");
        return mv;
    }

    @GetMapping("/")
    public ModelAndView index() {
        User user = loginService.getUserByName(SecurityUtils.getSubject().getPrincipal().toString());
        ModelAndView mv = new ModelAndView();
        if (null == user){
            mv.setViewName("/pages/page/login.html");
            return mv;
        }
        mv.addObject("userName",user.getUserName());
        mv.addObject("nickname",user.getNickname());
        mv.setViewName("/index.html");
        return mv;
    }


    @GetMapping("/pages/layouts/grid.html")
    public ModelAndView grid() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/pages/layouts/grid.html");
        return mv;
    }

    @GetMapping("/pages/projectManager/back-end-services.html")
    public ModelAndView backEndServices() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/pages/projectManager/back-end-services.html");
        return mv;
    }

    /**
     * 分片上传
     *
     * @return ResponseEntity<Void>
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Void> decrypt(HttpServletRequest request, @RequestParam(value = "file", required = false) MultipartFile file, Integer chunks, Integer chunk, String name, String guid,String path) throws IOException {
        //记录文件目标上传路径
        if (null==guid2path.get(guid)){
            guid2path.put(guid,path);
        }
        System.out.println(FILE_PATH);

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            if (file == null) {
                ;
            }
            System.out.println("guid:" + guid);
            if (chunks == null && chunk == null) {
                chunk = 0;
            }
            File outFile = FileUtil.touch(FILE_PATH+"/tmp/"+guid+'/'+chunk + ".part");
            if ("".equals(FILENAME)) {
                FILENAME = name;
            }
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 合并所有分片
     *
     * @throws Exception Exception
     */
    @GetMapping("/merge")
    @ResponseBody
    public void byteMergeAll(String guid) throws Exception {
        User user = loginService.getUserByName(SecurityUtils.getSubject().getPrincipal().toString());
        System.out.println("merge:"+guid);
        //查询文件目标路径
        String[] targetPath = guid2path.get(guid).split("\\.");
        guid2path.remove(guid);

        File file = FileUtil.touch(FILE_PATH+"/tmp/"+guid);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                File partFile = FileUtil.touch(FILE_PATH+"/"+user.getUserName()+ targetPath[1] + "/" + FILENAME);
                for (int i = 0; i < files.length; i++) {
                    File s = FileUtil.touch(FILE_PATH+"/tmp/"+guid+'/'+i + ".part");
                    FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                    FileUtils.copyFile(s, destTempfos);
                    destTempfos.close();
                }
                FileUtils.deleteDirectory(file);
                FILENAME = "";
            }

        }


    }

}
