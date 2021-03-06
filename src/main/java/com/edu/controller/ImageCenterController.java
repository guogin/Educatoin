package com.edu.controller;

import com.edu.dao.CustomerRepository;
import com.edu.dao.ImageCollectionRepository;
import com.edu.dao.ImageRepository;
import com.edu.dao.ProductRepository;
import com.edu.domain.*;
import com.edu.domain.dto.ImageContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ImageCenterController {
	@Autowired
	private CustomerRepository custRepo;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ImageCollectionRepository imageCollectionRepository;

	public final static String SESSION_OPENID_KEY = "openCode";

	public final static String IMAGE_COLLECTION_PATH = "/user/imagecollection";

	@GetMapping("/user/image")
	@ResponseBody
	public List<ImageContainer> getImages(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		Object openCodeObject = session.getAttribute("openCode");

		String openId = openCodeObject.toString();
		Customer customer = custRepo.findOneByOpenCode(openId);
		Set<Student> students = customer.getStudents();
		model.addAttribute("students", students);

		/**
		 * TODO: Below loop is wrong. We must show also the students list.
		 * So view also needs change.
		 */
		ArrayList<ImageContainer> imageList = new ArrayList<>();
		for (Student student : customer.getStudents()) {
			Set<Image> images = student.getImagesSet();
			ArrayList<ImageContainer> imagesContainer = images.stream()
					.sorted((x, y) -> (int)(y.getId()-x.getId()))
					.map(x -> new ImageContainer(x,student))
					.collect(Collectors.toCollection(ArrayList::new));

			imageList.addAll(imagesContainer);
		}
		return imageList;
	}

	@GetMapping(IMAGE_COLLECTION_PATH)
	private String doShowImageCollection(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();
		Object openCodeObject = session.getAttribute("openCode");
		String openId = openCodeObject.toString();

		Customer customer = custRepo.findOneByOpenCode(openId);
		for (Student student : customer.getStudents()) {
			Set<Image> images = student.getImagesSet();
			ArrayList<ImageContainer> imagesContainer = images.stream()
					.sorted((x, y) -> (int)(y.getId() - x.getId()))
					.map(x -> new ImageContainer(x))
					.collect(Collectors.toCollection(ArrayList::new));
	
			model.addAttribute("images", imagesContainer);
			model.addAttribute("code", openId);
		}
		return "derivation";
	}

	@PostMapping("/user/generateImagecollection")
	@ResponseBody
	public String createImageCollection(HttpServletRequest request, @RequestParam(value = "images") String images,
			Model model) {
		HttpSession session = request.getSession();
		Object openCodeObject = session.getAttribute("openCode");

		String openId = openCodeObject.toString();
		Customer customer = custRepo.findOneByOpenCode(openId);
		ImageCollection imageCollection = new ImageCollection();
		List<String> imagesWithId = Arrays.asList(images.split(","));
		Set<Image> imageList = new HashSet<>();
		for (String id : imagesWithId) {
			imageList.add(imageRepository.findOne(Long.parseLong(id)));
		}

		imageCollection.setImageCollection(imageList);
		List<Product> products = productRepository.getImageCollectionProductList();
		imageCollection.setProduct(products.get(0));
		imageCollection.setCollectionName("作品集");
		imageCollection.setCollectionDescription(imageList.size() + "幅作品");
		ImageCollection entity = imageCollectionRepository.save(imageCollection);
		customer.getCart().addImageCollection(entity);
		custRepo.save(customer);
		return "请到购物车查看生成的作品集";
	}
}
