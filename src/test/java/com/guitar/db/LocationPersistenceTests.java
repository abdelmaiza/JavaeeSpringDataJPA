package com.guitar.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.guitar.db.repository.LocationJpaRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.guitar.db.model.Location;
import com.guitar.db.repository.LocationRepository;

@ContextConfiguration(locations={"classpath:com/guitar/db/applicationTests-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class LocationPersistenceTests {
	//@Autowired
	//private LocationRepository locationRepository;

	@Autowired
	private LocationJpaRepository locationJpaRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	public void testJpaFind(){
		List<Location> locations = locationJpaRepository.findAll();
		assertNotNull(locations);
	}

	@Test
	@Transactional
	public void testSaveAndGetAndDelete() throws Exception {
		Location location = new Location();
		location.setCountry("Canada");
		location.setState("British Columbia");
		location = locationJpaRepository.saveAndFlush(location);
		
		// clear the persistence context so we don't return the previously cached location object
		// this is a test only thing and normally doesn't need to be done in prod code
		entityManager.clear();

		Location otherLocation = locationJpaRepository.findOne(location.getId());
		assertEquals("Canada", otherLocation.getCountry());
		assertEquals("British Columbia", otherLocation.getState());
		
		//delete BC location now
		locationJpaRepository.delete(otherLocation);
	}

	//@Test
	//public void testFindWithLike() throws Exception {
	//	List<Location> locs = locationRepository.getLocationByStateName("New");
	//	assertEquals(4, locs.size());
	//}

	@Test
	public void testFindWithLike() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateLike("New%");
		assertEquals(4, locs.size());
	}

    @Test
    public void testFindWithNotLike() throws Exception {
        List<Location> locs = locationJpaRepository.findByStateNotLike("New%");
        assertEquals(46, locs.size());
    }

	@Test
	public void testFindWithNotLikeOrderByStateAsc() throws Exception {
		List<Location> locs = locationJpaRepository.findByStateNotLikeOrderByStateAsc("New%");
		assertEquals(46, locs.size());

		locs.forEach(location -> {
			System.out.println(location.getState());
		});
	}


    @Test
    public void testFindStartingWith() throws Exception{
	    List<Location> locations = locationJpaRepository.findByStateStartingWith("New");
        assertEquals(4, locations.size());
    }

	@Test
	public void testFindIgnoreCaseStartingWith() throws Exception{
		List<Location> locations = locationJpaRepository.findByStateIgnoreCaseStartingWith("new");
		assertEquals(4, locations.size());
	}

	@Test
	public void testFindFirstIgnoreCaseStartingWith() throws Exception{
		Location location = locationJpaRepository.findFirstByStateIgnoreCaseStartingWith("a");
		assertEquals("Alabama", location.getState());
	}




    @Test
    public void testFindEndingWith() throws Exception{
        List<Location> locations = locationJpaRepository.findByStateEndingWith("New");
        assertEquals(0, locations.size());
    }


    @Test
    public void testFindContaining() throws Exception{
        List<Location> locations = locationJpaRepository.findByStateContaining("Miss");
        assertEquals(2, locations.size());
    }

    @Test
	@Transactional  //note this is needed because we will get a lazy load exception unless we are in a tx
	public void testFindWithChildren() throws Exception {
		Location arizona = locationJpaRepository.findOne(3L);
		assertEquals("United States", arizona.getCountry());
		assertEquals("Arizona", arizona.getState());
		
		assertEquals(1, arizona.getManufacturers().size());
		
		assertEquals("Fender Musical Instruments Corporation", arizona.getManufacturers().get(0).getName());
	}

	@Test
	public void testJpaAnd(){
		List<Location> locations = locationJpaRepository.findByStateAndCountry("Utah","United States");
		assertNotNull(locations);
		assertEquals("Utah", locations.get(0).getState());
	}

	@Test
	public void testJpaOr(){
		List<Location> locations = locationJpaRepository.findByStateOrCountry("Utah","Utah");
		assertNotNull(locations);
		assertEquals("Utah", locations.get(0).getState());
	}

	@Test
	public void testJpaIsOrEqual(){
		List<Location> locations = locationJpaRepository.findByStateIsOrCountryEquals("Utah","Utah");
		assertNotNull(locations);
		assertEquals("Utah", locations.get(0).getState());
	}

	@Test
	public void testJpaNot(){
		List<Location> locations = locationJpaRepository.findByStateNot("Utah");
		assertNotNull(locations);
		assertNotSame("Utah", locations.get(0).getState());
	}

}
