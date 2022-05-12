package exam.service.impl;

import exam.model.Shop;
import exam.model.Town;
import exam.model.dto.ShopSeedDto;
import exam.model.dto.ShopSeedRootDto;
import exam.repository.ShopRepository;
import exam.repository.TownRepository;
import exam.service.ShopService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final Validator validator;
    private final ModelMapper modelMapper;
    private final Unmarshaller unmarshaller;
    private final Path path = Path.of("src", "main", "resources", "files", "xml", "shops.xml");
    private final TownRepository townRepository;


    public ShopServiceImpl(ShopRepository shopRepository, ModelMapper modelMapper, TownRepository townRepository) throws JAXBException {
        this.shopRepository = shopRepository;
        this.modelMapper = modelMapper;
        this.townRepository = townRepository;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        JAXBContext context = JAXBContext.newInstance(ShopSeedRootDto.class);
        this.unmarshaller = context.createUnmarshaller();
    }


    @Override
    public boolean areImported() {
        return this.shopRepository.count() > 0;
    }

    @Override
    public String readShopsFileContent() throws IOException {
        return Files.readString(path);
    }

    @Override
    public String importShops() throws JAXBException, FileNotFoundException {

        List<String> result = new ArrayList<>();

        ShopSeedRootDto statImportRootDto = (ShopSeedRootDto) this.unmarshaller.unmarshal(new FileReader(path.toAbsolutePath().toString()));

        List<ShopSeedDto> shops = statImportRootDto.getShops();

        for (ShopSeedDto shop : shops) {

            Set<ConstraintViolation<ShopSeedDto>> validationErrors = validator.validate(shop);

            if (validationErrors.isEmpty()) {

                Shop shopToCheck = this.shopRepository.findByName(shop.getName());

                if (shopToCheck == null) {

                    Shop shopToAdd = this.modelMapper.map(shop, Shop.class);

                    Town town = this.townRepository.findByName(shopToAdd.getTown().getName());

                    shopToAdd.setTown(town);

                    String msg = "Successfully imported Shop " + shopToAdd.getName() + " - " + shopToAdd.getIncome();

                    result.add(msg);

                    this.shopRepository.save(shopToAdd);


                } else {
                    result.add("Invalid Shop");
                }


            } else {
                result.add("Invalid Shop");
            }


        }
        return String.join("\n", result);
    }
}









