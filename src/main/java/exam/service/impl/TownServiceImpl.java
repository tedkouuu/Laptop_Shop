package exam.service.impl;

import exam.model.Town;
import exam.model.dto.TownSeedDto;
import exam.model.dto.TownSeedRootDto;
import exam.repository.TownRepository;
import exam.service.TownService;
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
public class TownServiceImpl implements TownService {

    private final TownRepository townRepository;
    private final Validator validator;
    private final ModelMapper modelMapper;
    private final Unmarshaller unmarshaller;
    private final Path path = Path.of("src", "main", "resources", "files", "xml", "towns.xml");


    public TownServiceImpl(TownRepository townRepository, ModelMapper modelMapper) throws JAXBException {
        this.townRepository = townRepository;
        this.modelMapper = modelMapper;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        JAXBContext context = JAXBContext.newInstance(TownSeedRootDto.class);
        this.unmarshaller = context.createUnmarshaller();

    }


    @Override
    public boolean areImported() {
        return this.townRepository.count() > 0;
    }

    @Override
    public String readTownsFileContent() throws IOException {
        return Files.readString(path);
    }

    @Override
    public String importTowns() throws JAXBException, FileNotFoundException {

        List<String> result = new ArrayList<>();

        TownSeedRootDto offerSeedRootDto = (TownSeedRootDto) this.unmarshaller.unmarshal(new FileReader(path.toAbsolutePath().toString()));

        List<TownSeedDto> towns = offerSeedRootDto.getTowns();

        for (TownSeedDto town : towns) {

            Set<ConstraintViolation<TownSeedDto>> validationErrors = validator.validate(town);

            if (validationErrors.isEmpty()) {

                Town townToSave = this.modelMapper.map(town, Town.class);


                this.townRepository.save(townToSave);

                String msg = String.format("Successfully imported Town %s", townToSave.getName());

                result.add(msg);


            } else {
                result.add("Invalid town");
            }


        }

        return String.join("\n", result);

    }
}




















