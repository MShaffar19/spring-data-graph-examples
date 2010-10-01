package com.springone.myrestaurants.web;

import com.springone.myrestaurants.domain.Recommendation;
import com.springone.myrestaurants.domain.Restaurant;
import com.springone.myrestaurants.domain.UserAccount;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.stereotype.Controller;

@RequestMapping("/recommendations")
@Controller
public class RecommendationController extends BaseApplicationController {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long recommendationId,
    				   @ModelAttribute("currentUserAccountId") String userIdAsString, 
    				   Model model) {
		
		UserAccount account = this.userAccountRepository.findUserAccount(Long.parseLong(userIdAsString));
		Iterable<Recommendation> recs = account.getRecommendations();
		Recommendation foundRec = null;
		for (Recommendation recommendation : recs) {
			if (recommendation.getId().equals(recommendationId)) {
				foundRec = recommendation;
			}
		}
		RecommendationFormBean bean = new RecommendationFormBean();
		if (foundRec != null) {
			bean.setComments(foundRec.getComment());
			bean.setRating(foundRec.getStars());
			Restaurant r = foundRec.getRestaurant();
			bean.setName(r.getName());
		}
		model.addAttribute("recommendation", bean);
        return "recommendations/show";
    }
	
	
	@RequestMapping(method = RequestMethod.POST)
	public String create(RecommendationFormBean recommendationFormBean,
						 @ModelAttribute("currentUserAccountId") String userIdAsString,						 
						 BindingResult result,
						 Model model) {

		if (result.hasErrors()) {
			model.addAttribute("recommendation", recommendationFormBean);
			return "recommendations/create";
		}
		long restaurantId = recommendationFormBean.getRestaurantId();
		Restaurant restaurant = this.restaurantRepository.findRestaurant(restaurantId);
		UserAccount account = this.userAccountRepository.findUserAccount(Long.parseLong(userIdAsString));
		Recommendation recommendation = account.rate(restaurant,
				recommendationFormBean.getRating(),
				recommendationFormBean.getComments());
		model.addAttribute("recommendationId", recommendation.getId());
		// this.userAccountRepository.persist(account);
		// recommendation.persist();
		return "redirect:/recommendations/" + recommendation.getId();
	}

	@RequestMapping(value = "/{restaurantId}/{userId}", params = "form", method = RequestMethod.GET)
    public String createForm(@PathVariable("restaurantId") Long restaurantId, 
			 				 @PathVariable("userId") Long userId,
			                 Model model) {   
		RecommendationFormBean recBean = new RecommendationFormBean();
		Restaurant restaurant = this.restaurantRepository.findRestaurant(restaurantId);
		recBean.setRestaurantId(restaurantId);
		recBean.setName(restaurant.getName());
        model.addAttribute("recommendation", recBean);              
        //currentUserId is part of the implicit model due to spring security
        
        //model.addAttribute("userId", userId.toString());
        return "recommendations/create"; ///" + restaurantId + "/" + userId;
    }
	/*
	 * @RequestMapping(value = "/{id}", method = RequestMethod.GET) public
	 * String show(@PathVariable("id") Long id, Model model) {
	 * model.addAttribute("recommendation",
	 * Recommendation.findRecommendation(id)); model.addAttribute("itemId", id);
	 * return "recommendations/show"; }
	 */
}
