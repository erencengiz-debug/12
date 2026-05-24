package com.sase.app.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thymeleaf 3.1+'ta {@code #request} artık varsayılan bağlamda yoktur; layout menü aktivasyonları
 * için servlet yolunu model üzerinden verir ( {@code layout/base.html} ).
 */
@ControllerAdvice
public class LayoutNavAdvice {

    @ModelAttribute("navCtxPath")
    public String navContextPath(HttpServletRequest request) {
        String p = request.getContextPath();
        return p != null ? p : "";
    }

    @ModelAttribute("navRequestUri")
    public String navRequestUri(HttpServletRequest request) {
        String u = request.getRequestURI();
        return u != null ? u : "";
    }
}
