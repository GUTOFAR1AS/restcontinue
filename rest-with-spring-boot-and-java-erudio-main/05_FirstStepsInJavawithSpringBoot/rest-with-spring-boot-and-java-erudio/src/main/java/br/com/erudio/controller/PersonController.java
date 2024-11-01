package br.com.erudio.controller;

import br.com.erudio.model.Person;
import br.com.erudio.model.PersonDTO;
import br.com.erudio.mapper.PersonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/persons")
public class PersonController {

    @Autowired
    private br.com.erudio.service.PersonServices service;

    @Autowired
    private PersonMapper personMapper;

    @GetMapping(produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            "application/x-yaml"
    })
    @Operation(summary = "Retrieve all persons", description = "Returns a list of persons with HATEOAS links")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PersonDTO>> findAll(@RequestParam(required = false) String format) {
        List<Person> persons = service.findAll();
        List<PersonDTO> personDTOs = persons.stream()
                .map(personMapper::personToPersonDTO)
                .toList();

        for (PersonDTO personDTO : personDTOs) {
            addLinks(personDTO);
        }

        return ResponseEntity.ok(personDTOs);
    }

    @GetMapping(value = "/{id}", produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            "application/x-yaml"
    })
    @Operation(summary = "Retrieve person by ID", description = "Returns a person with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved person"),
            @ApiResponse(responseCode = "404", description = "Person not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PersonDTO> findById(@PathVariable Long id, @RequestParam(required = false) String format) {
        Person person = service.findById(id);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }

        PersonDTO personDTO = personMapper.personToPersonDTO(person);
        addLinks(personDTO);

        return ResponseEntity.ok(personDTO);
    }

    private void addLinks(PersonDTO personDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PersonController.class).findById(personDTO.getId(), null)).withSelfRel();
        Link updateLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PersonController.class).update(personDTO.getId(), null)).withRel("update");
        Link deleteLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PersonController.class).delete(personDTO.getId())).withRel("delete");
        personDTO.add(selfLink, updateLink, deleteLink);
    }

    @PostMapping
    @Operation(summary = "Create a new person", description = "Saves a new person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created person"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<Person> save(@RequestBody Person person) {
        Person personSaved = service.save(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(personSaved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a person", description = "Deletes a person with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted person"),
            @ApiResponse(responseCode = "404", description = "Person not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a person", description = "Updates a person with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated person"),
            @ApiResponse(responseCode = "404", description = "Person not found")
    })
    public ResponseEntity<Person> update(@PathVariable Long id, @RequestBody Person person) {
        Person updatedPerson = service.update(id, person);
        return updatedPerson != null ? ResponseEntity.ok(updatedPerson) : ResponseEntity.notFound().build();
    }

    @PostMapping("/convert")
    @Operation(summary = "Convert PersonDTO to Person", description = "Converts a PersonDTO to a Person and returns the DTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully converted person"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    public ResponseEntity<PersonDTO> convertPerson(@RequestBody PersonDTO personDTO) {
        Person person = personMapper.personDTOToPerson(personDTO);
        return ResponseEntity.ok(personMapper.personToPersonDTO(person));
    }
}
