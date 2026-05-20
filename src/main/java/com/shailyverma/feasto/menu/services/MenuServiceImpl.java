package com.shailyverma.feasto.menu.services;

import com.cloudinary.utils.ObjectUtils;
import com.shailyverma.feasto.category.entity.Category;
import com.shailyverma.feasto.category.repository.CategoryRepository;
import com.shailyverma.feasto.cloudinary.CloudinaryService;
import com.shailyverma.feasto.exceptions.BadRequestException;
import com.shailyverma.feasto.exceptions.NotFoundException;
import com.shailyverma.feasto.menu.dtos.MenuDTO;
import com.shailyverma.feasto.menu.entity.Menu;
import com.shailyverma.feasto.menu.repository.MenuRepository;
import com.shailyverma.feasto.response.Response;
import com.shailyverma.feasto.review.dtos.ReviewDTO;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;



@Slf4j
@RequiredArgsConstructor
@Service
public class MenuServiceImpl implements MenuService{


    private final CloudinaryService cloudinaryService;
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;




    @Override
    public Response<MenuDTO> createMenu(MenuDTO menuDTO) {
        log.info("INSIDE createMenu()");
        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        String imageUrl = null;

        MultipartFile imageFile = menuDTO.getImageFile();

        if (imageFile == null || imageFile.isEmpty()) {
            throw new BadRequestException("Menu Image is required");
        }


        imageUrl = cloudinaryService.uploadFile(imageFile);

        Menu menu = Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .price(menuDTO.getPrice())
                .imageUrl(imageUrl)
                .category(category)
                .build();

        Menu savedMenu = menuRepository.save(menu);

        return Response.<MenuDTO>builder()
                        .statusCode(HttpStatus.OK.value())
                                .message("Menu created successfully")
                                        .data(modelMapper.map(savedMenu,MenuDTO.class))
                .build();
    }


    @Override
    public Response<MenuDTO> updateMenu(MenuDTO menuDTO) {
        log.info("INSIDE updateMenu()");
        Menu existingMenu = menuRepository.findById(menuDTO.getId())
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        String imageUrl = existingMenu.getImageUrl();
        MultipartFile imageFile = menuDTO.getImageFile();


        if (imageFile != null && !imageFile.isEmpty()) {

            imageUrl = cloudinaryService.uploadFile(imageFile);
        }
        if (menuDTO.getName() != null && !menuDTO.getName().isBlank())
            existingMenu.setName(menuDTO.getName());

        if (menuDTO.getDescription() != null && !menuDTO.getDescription().isBlank())
            existingMenu.setDescription(menuDTO.getDescription());

        if (menuDTO.getPrice() != null)
            existingMenu.setPrice(menuDTO.getPrice());

        existingMenu.setImageUrl(imageUrl);
        existingMenu.setCategory(category);

        Menu updatedMenu = menuRepository.save(existingMenu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu updated successfully")
                .data(modelMapper.map(updatedMenu, MenuDTO.class))
                .build();
    }


    @Override
    public Response<MenuDTO> getMenuById(Long id) {
        log.info("INSIDE getMenuById()");
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found "));

        MenuDTO menuDTO = modelMapper.map(existingMenu, MenuDTO.class);

// Sort the reviews in descending order
        if (menuDTO.getReviews() != null) {
            menuDTO.getReviews().sort(Comparator.comparing(ReviewDTO::getId).reversed());
        }

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu retrieved successfully")
                .data(menuDTO)
                .build();
    }

    @Override
    public Response<?> deleteMenu(Long id) {

        log.info("Inside deleteMenu()");

        Menu menuToDelete = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        // Delete the image from Cloudinary if it exists
        String imageUrl = menuToDelete.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {

                String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));


                String fullPublicId = "menus/" + publicId;

                cloudinaryService.deleteFile(fullPublicId);
                log.info("Deleted image from Cloudinary: " + fullPublicId);
            } catch (Exception e) {
                log.error("Failed to delete image from Cloudinary", e);
                // Optional: decide if you want to throw an error or continue deleting the DB record
            }
        }

        menuRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu deleted successfully")
                .build();
    }


    @Override
    public Response<List<MenuDTO>> getMenus(Long categoryId, String search) {
        log.info("INSIDE getMenus()");
            Specification<Menu> spec = buildSpecification(categoryId, search);

            Sort sort = Sort.by(Sort.Direction.DESC, "id");

            List<Menu> menuList = menuRepository.findAll(spec, sort);

            List<MenuDTO> menuDTOS = menuList.stream()
                    .map(menu -> modelMapper.map(menu, MenuDTO.class))
                    .toList();

            return Response.<List<MenuDTO>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Menus retrieved")
                    .data(menuDTOS)
                    .build();
        }


    private Specification<Menu> buildSpecification(Long categoryId, String search) {
        return (Root<Menu> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // List to accumulate all WHERE conditions
            List<Predicate> predicates = new ArrayList<>();

            // Add category filter if categoryId is provided
            if (categoryId != null) {
                predicates.add(cb.equal(
                        root.get("category").get("id"),
                        categoryId
                ));
            }

            if (search != null && !search.isBlank()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(
                                cb.lower(root.get("name")),
                                searchTerm
                        ),
                        cb.like(
                                cb.lower(root.get("description")),
                                searchTerm
                        )
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    }
