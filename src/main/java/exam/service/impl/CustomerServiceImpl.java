package exam.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exam.model.Customer;
import exam.model.Town;
import exam.model.dto.CustomersSeedDto;
import exam.repository.CustomerRepository;
import exam.repository.TownRepository;
import exam.service.CustomerService;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final TownRepository townRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final Validator validator;

    public CustomerServiceImpl(CustomerRepository customerRepository, TownRepository townRepository, ModelMapper modelMapper) {
        this.customerRepository = customerRepository;
        this.townRepository = townRepository;
        this.modelMapper = modelMapper;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();

    }

    @Override
    public boolean areImported() {
        return this.customerRepository.count() > 0;
    }

    @Override
    public String readCustomersFileContent() throws IOException {

        //src/main/resources/files/json/customers.json
        Path path = Path.of("src", "main", "resources", "files", "json", "customers.json");

        return Files.readString(path);
    }

    @Override
    public String importCustomers() throws IOException {

        List<String> result = new ArrayList<>();

        String json = readCustomersFileContent();

        CustomersSeedDto[] customersSeedDtos = this.gson.fromJson(json, CustomersSeedDto[].class);

        for (CustomersSeedDto customer : customersSeedDtos) {

            Set<ConstraintViolation<CustomersSeedDto>> validationErrors = validator.validate(customer);

            if (validationErrors.isEmpty()) {

                Customer customerToCheck = this.customerRepository.findByEmail(customer.getEmail());

                if (customerToCheck == null) {

                    Customer customerToAdd = this.modelMapper.map(customer, Customer.class);

                    Town town = this.townRepository.findByName(customerToAdd.getTown().getName());

                    customerToAdd.setTown(town);

                    String msg = String.format("Successfully imported Customer %s %s - %s",customerToAdd.getFirstName(),customerToAdd.getLastName(),customerToAdd.getEmail());

                    result.add(msg);

                    this.customerRepository.save(customerToAdd);


                } else {
                    result.add("Invalid Customer");
                }


            } else {
                result.add("Invalid Customer");
            }
        }
        return String.join("\n", result);

    }
}

















