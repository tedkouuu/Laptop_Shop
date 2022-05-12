package exam.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exam.model.Laptop;
import exam.model.Shop;
import exam.model.dto.LaptopSeedDto;
import exam.repository.LaptopRepository;
import exam.repository.ShopRepository;
import exam.service.LaptopService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class LaptopServiceImpl implements LaptopService {

    private final LaptopRepository laptopRepository;
    private final ShopRepository shopRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final Validator validator;

    public LaptopServiceImpl(LaptopRepository laptopRepository, ShopRepository shopRepository, ModelMapper modelMapper) {
        this.laptopRepository = laptopRepository;
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();

    }

    @Override
    public boolean areImported() {
        return this.laptopRepository.count() > 0;
    }

    @Override
    public String readLaptopsFileContent() throws IOException {
        Path path = Path.of("src", "main", "resources", "files", "json", "laptops.json");

        return Files.readString(path);

    }

    @Override
    public String importLaptops() throws IOException {

        List<String> result = new ArrayList<>();

        String json = readLaptopsFileContent();

        LaptopSeedDto[] laptopSeedDtos = this.gson.fromJson(json, LaptopSeedDto[].class);

        for (LaptopSeedDto laptopSeedDto : laptopSeedDtos) {

            Set<ConstraintViolation<LaptopSeedDto>> validationErrors = validator.validate(laptopSeedDto);

            if (validationErrors.isEmpty()) {

                Laptop checkLaptop = this.laptopRepository.findByMacAddress(laptopSeedDto.getMacAddress());

                if (checkLaptop == null) {

                    Laptop laptop = this.modelMapper.map(laptopSeedDto, Laptop.class);

                    Shop shop = this.shopRepository.findByName(laptop.getShop().getName());

                    laptop.setShop(shop);

                    this.laptopRepository.save(laptop);

                    String msg = String.format("Successfully imported Laptop %s - %.2f - %d - %d",
                            laptop.getMacAddress(), laptop.getCpuSpeed(), laptop.getRam(), laptop.getStorage());

                    result.add(msg);

                } else {
                    result.add("Invalid Laptop");
                }


            } else {
                result.add("Invalid Laptop");
            }


        }

        return String.join("\n", result);

    }

    @Override
    public String exportBestLaptops() {

        List<String> result = new ArrayList<>();

        Set<Laptop> laptops = this.laptopRepository.exportBestLaptops();

        StringBuilder builder = new StringBuilder();

        for (Laptop laptop : laptops) {

            String cpu = String.format("*Cpu speed - %.2f", laptop.getCpuSpeed());


            builder.append("Laptop - ").append(laptop.getMacAddress()).append("\n");
            builder.append(cpu).append("\n");
            builder.append("**Ram - ").append(laptop.getRam()).append("\n");
            builder.append("***Storage - ").append(laptop.getStorage()).append("\n");
            builder.append("****Price - ").append(laptop.getPrice()).append("\n");
            builder.append("#Shop name - ").append(laptop.getShop().getName()).append("\n");
            builder.append("##Town - ").append(laptop.getShop().getTown().getName()).append("\n");

            result.add(builder.toString());

        }


        return String.join("\n", result);
    }

}
















