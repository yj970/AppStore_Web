package servlet;

import bean.AppBean;
import bean.CommentBean;
import com.alibaba.fastjson.JSON;
import db.DButil;
import util.L;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppInfoServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action.equals("getAppInfo")) {
            String packageId = request.getParameter("packageId");
            getAppInfo(packageId, response);
        } else if (action.equals("getCommentList")) {
            String packageId = request.getParameter("packageId");
            String page = request.getParameter("page");
            getCommentList(packageId, Integer.parseInt(page), response);
        } else if (action.equals("commitComment")) {
            String packageId = request.getParameter("packageId");
            String comment = request.getParameter("comment");
            String userId = "123";// todo 暂时写死
            commitComment(userId, packageId, comment, response);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    // 从数据库获取app信息
    private void getAppInfo(String packageId, HttpServletResponse response) throws IOException {
        L.d("开始执行getAppInfo...");
        // 从数据库获取app信息
        Connection conn = DButil.getConnection();
        try {
            String sql = "select * from appInfo where packageId = "+packageId;
            PreparedStatement ptmt =  (PreparedStatement) conn.prepareStatement(sql);
            ResultSet rs = ptmt.executeQuery();
            rs.next();
            AppBean bean = null;
                bean = new AppBean();
                bean.setPackageId(rs.getString("packageId"));
                bean.setName(rs.getString("name"));
                bean.setDownloadUrl(rs.getString("downloadUrl"));
                bean.setLogoUrl(rs.getString("logoUrl"));
                bean.setDescribe(rs.getString("describe"));
            // 对象转json
            String json = JSON.toJSONString(bean);
            response.getWriter().print(json);
            L.d("执行getAppInfo...成功");
        } catch (SQLException e) {
            e.printStackTrace();
            L.d("执行getAppInfo...失败："+e.getMessage());
        }
    }

    // 从数据库获取评论信息
    private void getCommentList(String packageId, int page, HttpServletResponse response) throws IOException {
        L.d("开始执行getCommentList...");
        // 从数据库获取app信息
        Connection conn = DButil.getConnection();
        try {
            String sql = "select * from comment where packageId = "+packageId +" limit "+(page-1)*10+",10" ;
            PreparedStatement ptmt =  (PreparedStatement) conn.prepareStatement(sql);
            ResultSet rs = ptmt.executeQuery();
            List<CommentBean> list = new ArrayList<>(10);
            while (rs.next()) {
                CommentBean bean = null;
                bean = new CommentBean();
                bean.setId(rs.getString("id"));
                bean.setPackageId(rs.getString("packageId"));
                bean.setComment(rs.getString("comment"));
                bean.setUserId(rs.getString("userId"));
                bean.setTime(rs.getString("time"));
                list.add(bean);
            }
            // 对象转json
            String json = JSON.toJSONString(list);
            response.getWriter().print(json);
            L.d("执行getCommentList...成功");
        } catch (SQLException e) {
            e.printStackTrace();
            L.d("执行getCommentList...失败："+e.getMessage());
        }
    }

    // 提交评论
    private void commitComment(String userId, String packageId, String comment, HttpServletResponse response) throws IOException {
        L.d("开始执行commitComment...");
        // 从数据库获取app信息
        Connection conn = DButil.getConnection();
        try {
            String sql = "insert into comment(userId, packageId, comment, time) values(?,?,?,?) ";
            PreparedStatement ptmt =  (PreparedStatement) conn.prepareStatement(sql);
            ptmt.setString(1, userId);
            ptmt.setString(2, packageId);
            ptmt.setString(3, comment);
            ptmt.setDate(4, new Date(System.currentTimeMillis()));
            ptmt.execute();
            response.getWriter().print("ok");
            L.d("执行commitComment...成功");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().print(e.getMessage());
            L.d("执行commitComment...失败："+e.getMessage());
        }
    }

}
