package com.example.pickitup.controller;

import com.example.pickitup.domain.vo.ProductQnaCriteria;
import com.example.pickitup.domain.vo.dto.*;
import com.example.pickitup.domain.vo.product.productFile.ProductVO;
import com.example.pickitup.domain.vo.product.productQna.ProductQnaCommentVO;
import com.example.pickitup.domain.vo.product.productQna.ProductQnaVO;
import com.example.pickitup.domain.vo.product.productReview.ProductReviewVO;
import com.example.pickitup.domain.vo.user.JjimVO;
import com.example.pickitup.domain.vo.user.OrderVO;
import com.example.pickitup.domain.vo.user.UserVO;
import com.example.pickitup.service.TempAdminService;
import com.example.pickitup.service.TempUserSerivce;
import com.example.pickitup.service.product.productFile.ProductFileService;
import com.example.pickitup.service.product.productFile.ProductService;
import com.example.pickitup.service.product.productQna.ProductQnaCommentService;
import com.example.pickitup.service.product.productQna.ProductQnaService;
import com.example.pickitup.service.product.productReview.ProductReviewService;
import com.example.pickitup.service.user.JjimService;
import com.example.pickitup.service.user.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/store/*")
@RequiredArgsConstructor
public class StoreController {
    private final ProductService productService;
    private final ProductReviewService productReviewService;
    private final ProductQnaService productQnaService;
    private final ProductQnaCommentService productQnaCommentService;
    private final JjimService jjimService;
    private final OrderService orderService;
    private final TempUserSerivce tempUserSerivce;
    private final TempAdminService tempAdminService;
    // ????????? ???????????????
    @GetMapping("/main")
    public void storeMain(String category,Model model){
        if(category == ""){
            category = null;
        }
        model.addAttribute("productsCount",productService.count());
        model.addAttribute("productlist",productService.getList(category));
    }

    @ResponseBody
    @PostMapping("/main")
    public List<ProductVO> storepostMain(String category,Model model){
        if(category == ""){
            category = null;
        }
        model.addAttribute("productsCount",productService.count());
        model.addAttribute("productlist",productService.getList(category));
        return productService.getList(category);
    }

    // ????????? ???????????????
    @GetMapping("/detail")
    public String storeDetail(Long num ,Model model){
//        // ?????? ???????????? ????????? num ???????????????
//        model.addAttribute("user", tempUserSerivce.readUserInfo(22L));
        model.addAttribute("jjimCount",jjimService.count(num));
        model.addAttribute("count",productReviewService.count(num));
        model.addAttribute("product",productService.getDetail(num));
        return "/store/detail";
    }

    // ????????? ?????? ??????
    @ResponseBody
    @GetMapping("/reviewLists/{productNum}")
    public List<ProductReviewVO> reviewLists(@PathVariable("productNum") Long productNum){
        // ?????? ???????????? ????????? num ???????????????
       return productReviewService.getList(productNum);
    }

    //?????? ?????? ????????????
    @ResponseBody
    @GetMapping("/userInfo")
    public UserVO userinfo(Long userNum){
        return tempUserSerivce.readUserInfo(userNum);
    }


    // ?????? ?????? ?????? ?????????
    @GetMapping("/goReviewList/{productNum}")
    public String goReviewList(@PathVariable("productNum") Long productNum,Model model){
//        // ?????? ???????????? ????????? num ???????????????
//        model.addAttribute("user",tempUserSerivce.readUserInfo(22L));
        model.addAttribute("products",productService.getDetail(productNum));
        model.addAttribute("productNum",productNum);
        model.addAttribute("reviews",productReviewService.getList(productNum));
        return "/store/reviewList";
    }

    // ????????? ?????? ??????
    @GetMapping("/reviewWrite")
    public void reviewWrite(Long num, Model model){
        model.addAttribute("product",productService.getDetail(num));
    }

    // ????????? ?????? ?????? ???
    @PostMapping("/reviewWrite")
    public RedirectView reviewWriteForm(ProductReviewVO productReviewVO, RedirectAttributes rttr){
//        model.addAttribute("user", productNum); ????????? ?????? ???????????????.?? ???????????
        log.info("===================================");
        log.info("?????????????????????"+productReviewVO.getProductNum());
        log.info("===================================");
        productReviewVO.setUserNum(22L);
        productReviewService.insert(productReviewVO);
        rttr.addAttribute("num",productReviewVO.getProductNum());
        return new RedirectView("/store/detail");
//
    }

    //????????? ?????? ??????
    @GetMapping("/reviewModify")
    public void reviewModify(Long num,Model model){
        model.addAttribute("review",productReviewService.read(num));
        model.addAttribute("product", productService.getDetail(productReviewService.read(num).getProductNum()));
    }
    //????????? ?????? ?????? ???
    @PostMapping("/reviewModify")
    public String reviewModify(ProductReviewVO productReviewVO, Model model){
//        model.addAttribute("user", productNum); ????????? ?????? ???????????????.?? ???????????
        productReviewVO.setUserNum(22L);
        productReviewService.modify(productReviewVO);
        return storeDetail(productReviewVO.getProductNum(), model);
    }

    // ????????? ?????? ??????
    @ResponseBody
    @GetMapping("/reviewDelete")
    public void reviewDelete(Long num){
        productReviewService.delete(num);
    }

    // ????????? ?????? ??????
    @ResponseBody
    @GetMapping("/qnaList/{productNum}/{pageNum}")
    public ProductQnaPageDTO qnaList(@PathVariable("pageNum") int pageNum, @PathVariable("productNum") Long productNum){
       return new ProductQnaPageDTO(productQnaService.getList(new ProductQnaCriteria(pageNum,5),productNum),productQnaService.count(productNum));
    }

    // ????????? ?????? ??????
    @GetMapping("/qnaWrite")
    public void qnaWrite(Long productNum, Model model){
        //?????? ????????? ?????? ????????????
        model.addAttribute("productNum",productNum);
    }

    // ????????? ?????? ?????? ???
    @PostMapping("/qnaWrite")
    public String qnaWriteForm(ProductQnaVO productQnaVO, AdminQnaDTO adminQnaDTO, Model model){
        productQnaService.register(productQnaVO);
        tempAdminService.qnaStoreSave(adminQnaDTO);
        return storeDetail(productQnaVO.getProductNum(), model);
    }

    // ????????? ?????? ??????
    @GetMapping("/qnaModify")
    public void qnaModify(Long num, Model model){
        model.addAttribute("qnaDetail",productQnaService.read(num));
    }
    // ????????? ?????? ?????????
    @PostMapping("/qnaModify")
    public String qnaModifyAction(ProductQnaVO productQnaVO, Model model){
        productQnaService.update(productQnaVO);
        return storeDetail(productQnaVO.getProductNum(), model);
    }

    // ????????? ?????? ??????
    @ResponseBody
    @GetMapping("/qnaDelete")
    public void qnaDelete(Long num){
        productQnaService.remove(num);
    }



    // ????????? ?????? ?????? ?????????
    @ResponseBody
    @GetMapping("/qnaCommentList/{qnaNum}")
    public List<ProductQnaCommentVO> qnaCommentList(@PathVariable("qnaNum") Long qnaNum){
        return productQnaCommentService.getList(qnaNum);
    }

    // ????????? ?????? ?????? ?????? (????????? ??????)
    @ResponseBody
    @PostMapping(value = "/qnaCommentWrite", consumes = "application/json")
    public String qnaCommentWrite(@RequestBody ProductQnaCommentVO productQnaCommentVO)  throws UnsupportedEncodingException {
        //(?????????) ????????? ?????? ????????????
        productQnaCommentService.register(productQnaCommentVO);
        return "success";
    }
//
    // ????????? ?????? ?????? ??????
    // ????????? ????????? ?????? ???????????????
    @ResponseBody
    @GetMapping("/qnaCommentDelete")
    public String qnaCommentDeleteForm(Long qnaCommentNum){
        productQnaCommentService.delete(qnaCommentNum);
        return "success";
    }


    // ????????? ?????? ?????? ??????
    // ????????? ????????? ?????? ???????????????
    @ResponseBody
    @PostMapping("/qnaCommentUpdate")
    public String qnaCommentUpdateForm(ProductQnaCommentVO productQnaCommentVO) throws UnsupportedEncodingException {
        log.info("=============================================");
        log.info("=============================================");
        log.info("=============================================");
        log.info("=============================================");
        log.info("=============================================");
        productQnaCommentService.update(productQnaCommentVO);
        return "success";
    }

    @GetMapping("/payment")
    public String payment(Long num){
        orderService.payment(num);
        return "store/payment";
    }






    //??????????????? ?????? ??????




    // ????????? ?????? ?????? ??????
    @PostMapping("/payment")
    public void paymentForm(ProductDTO productDTO, ProductVO productVO,Model model){
        model.addAttribute("product", productVO);
        model.addAttribute("productinfo",productDTO);
    }
//    @PostMapping("/payment")
//    public void paymentForm(ProductDTO productDTO, ProductVO productVO,Model model){
//        model.addAttribute("product", productVO);
//        model.addAttribute("productinfo",productDTO);
//    }

    // ????????? ?????? ??? ?????? ??????
    @PostMapping("/itemChoose")
    public void itemChoose(UserVO userVO, ProductVO productVO,Model model){
        userVO = tempUserSerivce.readUserInfo(22L);

        model.addAttribute("product",productVO);
        model.addAttribute("user", userVO);
    }

    // ?????? ?????? ??? ????????????
    @PostMapping("/buyProductDetail")
    public void buyProductDetail(OrderUserDTO orderUserDTO, ProductVO productVO, UserVO userVO, ProductDTO productDTO, String addressComment, Model model){
        Long num = 22L; // ????????????
        orderUserDTO.setUserNum(num);
        orderUserDTO.setNickName(productDTO.getNickName());
        orderUserDTO.setPhone(productDTO.getPhone());
        orderUserDTO.setCounting(Long.parseLong(productDTO.getTotalitems()));
        orderUserDTO.setTotal(Long.parseLong(productDTO.getTotalpayment()));
        orderUserDTO.setProductName(productDTO.getItemname());
        orderUserDTO.setAddressComment(productDTO.getAddressComment());
        orderUserDTO.setAddress(productDTO.getAddress());
        orderUserDTO.setAddressDetail(productDTO.getAddressDetail());
        tempUserSerivce.orderStore(orderUserDTO);

        userVO = tempUserSerivce.readUserInfo(num);
        String point = Long.toString(Long.parseLong(userVO.getPoint()) - Long.parseLong(productDTO.getTotalpayment())); //????????? ?????????
        tempUserSerivce.userPointMinus(num, point);

        String itemname = productDTO.getItemname();
        tempUserSerivce.getDetailByName(itemname); // ?????? ??????
        Long stock = tempUserSerivce.getDetailByName(itemname) - Long.parseLong(productDTO.getTotalitems()); //????????? ??????
        tempUserSerivce.productMinus(itemname, stock);





        model.addAttribute("addressComment", addressComment);
        model.addAttribute("userinfo",userVO);
        model.addAttribute("orderinfo",orderUserDTO);
        model.addAttribute("product",productDTO);

    }

    private String getFolder(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        return sdf.format(date);
    }

    //??? ??????
    @ResponseBody
    @GetMapping("/jjim")
    public List<JjimVO> listJjim(){
       return jjimService.getList();
    }


    // ?????????
    @ResponseBody
    @PostMapping("/jjim")
    public void addJjim(JjimVO jjimVO){
        jjimService.register(jjimVO);
    }

    // ?????????
    @ResponseBody
    @DeleteMapping("/jjim")
    public void removeJjim(JjimVO jjimVO){
        jjimService.remove(jjimVO);
    }

    //??? ??????
    @ResponseBody
    @GetMapping("jjimCount")
    public int count(Long productNum){
        return jjimService.count(productNum);
    }

}
