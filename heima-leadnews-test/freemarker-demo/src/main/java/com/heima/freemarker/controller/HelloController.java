package com.heima.freemarker.controller;

import com.heima.freemarker.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class HelloController {

    @GetMapping("/basic")
    public String hello(Model model) {

        model.addAttribute("name", "FreeMarker");

        Student s = new Student();
        s.setAge(18);
        s.setName("张三");
        model.addAttribute("stu", s);

        return "01-basic";
    }


    @GetMapping("/list")
    public String list(Model model) {

        //------------------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        //向model中存放List集合数据
        model.addAttribute("stus",stus);
        
        Map<String, Object> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        model.addAttribute("stuMap",stuMap);

        model.addAttribute("today",new Date());

        return "02-list";
    }
}
