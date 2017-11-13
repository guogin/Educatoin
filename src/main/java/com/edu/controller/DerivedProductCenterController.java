package com.edu.controller;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.edu.dao.*;
import com.edu.domain.*;
import com.edu.domain.dto.DerivedProduct;
import com.edu.domain.dto.ImageContainer;
import com.edu.domain.dto.ProductContainer;
import com.edu.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DerivedProductCenterController {
    @Autowired
    private CustomerRepository custRepo;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private DerivedProductRepository derivedProductRepository;

    @Autowired
    private ProductRepository productRepository;

    public final static String SESSION_OPENID_KEY = "openCode";
    public final static String RELATED_IMAGE_PATH = "/user/relatedimage";
    public final static String RELATED_IMAGE_CALLBACK_PATH = "/user/relatedimage/cb";
    
    public final static String PATH_WORKS = "/derivation/works";
    public final static String PATH_GIFTS = "/derivation/gift";
    public final static String PATH_FRAME = "/derivation/frame";
    public final static String PATH_LITER = "/derivation/literature";
    
    @GetMapping(PATH_WORKS)
    public String navWorksPage() {
        return "derivation_works";
    }
    @GetMapping(PATH_GIFTS)
    public String navGiftsPage() {
        return "derivation_gift";
    }
    @GetMapping(PATH_FRAME)
    public String navFramePage() {
        return "derivation_frame";
    }
    @GetMapping(PATH_LITER)
    public String navLiterPage() {
        return "derivation_liter";
    }
    

    @GetMapping(RELATED_IMAGE_PATH)
    private String doShowRelatedImage(HttpServletRequest request, Model model) {
    	String openId = (String) request.getSession().getAttribute(Constant.SESSION_OPENID_KEY);
        if (openId == null) {
            return "error_500";
        }

        Customer customer = custRepo.findOneByOpenCode(openId);
        ArrayList<ImageContainer> imageContainer = new ArrayList<>();
        for (Student student : customer.getStudents()) {
            Set<Image> images = student.getImagesSet();
            ArrayList<ImageContainer> imagesContainer = images.stream()
                    .sorted((x, y) -> y.getDate().compareTo(x.getDate()))
                    .map(x -> new ImageContainer(x.getId(), x.getImageName(), x.getDate(), x.getCourse(),
                            "/Images/" + x.getId(), "/Images/" + x.getId() + "/thumbnail"))
                    .collect(Collectors.toCollection(ArrayList::new));
            
            imageContainer.addAll(imagesContainer);
        }
        model.addAttribute("code", openId);
        model.addAttribute("images", imageContainer.stream().sorted((x, y) -> y.getDate().compareTo(x.getDate()))
				.collect(Collectors.toCollection(ArrayList::new)));
        return "user_imagelist";
    }

    @GetMapping("/user/derivedproduct")
    public String getDerivedProduct(HttpServletRequest request, @RequestParam(value = "imgcontainer") String imageId,
                                    Model model) {
        HttpSession session = request.getSession();
        Object openCodeObject = session.getAttribute("openCode");

        if (null == openCodeObject) {
            return "error_500";
        }

        String openId = openCodeObject.toString();
        Customer customer = custRepo.findOneByOpenCode(openId);
        if (customer == null) {
            Customer newCustomer = new Customer();
            newCustomer.setOpenCode(openId);
            model.addAttribute("customer", newCustomer);
            return "user_signup";
        } else {
            Image image = imageRepository.findOne(Long.parseLong(imageId));
            model.addAttribute("image", new ImageContainer(image.getId(), image.getImageName(), image.getDate(),
                    image.getCourse(), "/Images/" + image.getId(), "/Images/" + image.getId() + "/thumbnail"));
            ArrayList<ProductContainer> products = productRepository.getDerivedProductList().stream()
                    .map(x -> new ProductContainer(x.getProductName(), x.getProductCategory().getCategoryName(),
                            x.getProductPrice(), x.getProductDescription(),
                            "/Images/" + x.getProductImages().stream().findFirst().get().getId(), 1, x.getId(), 2))
                    .collect(Collectors.toCollection(ArrayList::new));
            model.addAttribute("products", products);
            return "derivation";
        }
    }

    @PostMapping("/user/createderivedproduct")
    @ResponseBody
    public String createDerivedProduct(HttpServletRequest request, @RequestParam(value = "productid") String productid,
                                       @RequestParam(value = "imageid") String imageid, Model model) {
        HttpSession session = request.getSession();
        Object openCodeObject = session.getAttribute("openCode");

        if (null == openCodeObject) {
            return "error_500";
        }

        String openId = openCodeObject.toString();
        Customer customer = custRepo.findOneByOpenCode(openId);
        if (customer == null) {
            Customer newCustomer = new Customer();
            newCustomer.setOpenCode(openId);
            model.addAttribute("customer", newCustomer);
            return "user_signup";
        } else {
            DerivedProduct derivedProduct = new DerivedProduct();
            derivedProduct.setProduct(productRepository.findOne(Long.parseLong(productid)));
            derivedProduct.setImage(imageRepository.findOne(Long.parseLong(imageid)));
            customer.getCart().addDerivedProducts(derivedProductRepository.save(derivedProduct));
            custRepo.save(customer);
            return "请至购物车查看";
        }
    }
}
