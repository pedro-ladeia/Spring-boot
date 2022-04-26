package com.api.parkingControl.controler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingControl.dtos.ParkingSpotDto;
import com.api.parkingControl.model.ParkingSpotModel;
import com.api.parkingControl.services.ParkingSpotService;

@RestController //Controlador 
@CrossOrigin(origins = "x", maxAge = 3600) //Poder ser acessado de qualquer lugar
@RequestMapping("/parking-spot") //URI
public class ParkingSpotController {
	
	@Autowired //Ponto de injeção
	ParkingSpotService parkingSpotService;

	@PostMapping //Méotodo Post
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
		/*Criando um ResponseEntity de Objetos, que recebe como parâmetro o dto, utilizando das notações RequestBody para
		receber os dados via JSON e o Valid para validação dos dados.*/
		
		//Verificações criadas pela regra de negócio.
		//OBS: Para ampliar a maturidade da aplicação, criar uma classe somnete para essas verificações
		
		if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro. License Plate Car is already in use");
		}
		
		if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro. Parking spot is already in use");
		}
		
		if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro. Parking spot is already in use for this apartment and block");
		}
		
		var parkingSpotModel = new ParkingSpotModel(); //Instanciando o model utilizando o var
		BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel); //Converter dto para model antes de salvar
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC"))); // Setando o local time*/
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
		/* Retornando uma resposta http, utilizando o ResponseEntity, com o status Created (201) e no corpo, 
		o model que foi salvo no service*/
		
}
	
	
	//Método que retorna todos os registros no banco
	
	@GetMapping
	public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots() { //O método retorna uma lista de modelos pelo ResponseEntity
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
		//Retornando o staus Ok (200) e no corpo retorna todos os registros através do método findAll originário do jpa
	}	


	
	@GetMapping("/{id}") //Método get que recebe como parâmetro o id para retornar todo o registro
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id) {
		// PathVariable para receber o id passado na url, value tem que ser igual ao nome referenciado na url, e o tipo
		
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		//Utilizando o optional para evitar o NullPointerException
		if(!parkingSpotModelOptional.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not Found");
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());	
		}


	@DeleteMapping("/{id}") //Método Delete que recebe como parâmetro o id para deletar
	public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id) { //Método semelhante ao de procurar pelo id
		
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if(!parkingSpotModelOptional.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not Found");
		parkingSpotService.delete(parkingSpotModelOptional.get());
		return ResponseEntity.status(HttpStatus.OK).body("Register deleted successfully");
		
	}

}
