package org.nicta.lr.recommender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nicta.lr.util.Constants;
import org.nicta.lr.util.UserUtil;

public class BaselineRecommender extends Recommender
{
	String type;
	
	public BaselineRecommender(Map<Long, Set<Long>> linkLikes, Map<Long, Double[]> userFeatures, Map<Long, Double[]> linkFeatures, Map<Long, Map<Long, Double>> friends, String type)
	{	
		super(linkLikes, userFeatures, linkFeatures, friends);
		this.type = type;
	}
	
	public Map<Long, Double[]> getPrecisionRecall(Map<Long, Set<Long>> testData, int boundary)
	{
		HashMap<Long, Double[]> precisionRecalls = new HashMap<Long, Double[]>();
		
		HashSet<Long> combinedTest = new HashSet<Long>();
		for (long userId : testData.keySet()) {
			combinedTest.addAll(testData.get(userId));
		}
		
		int user = 0;
		for (long userId : testData.keySet()) {
			user++;
			System.out.println("User: " + user);
			Set<Long> testLinks = testData.get(userId);
			Map<Long, Double> friends = friendships.get(userId);
			
			ArrayList<Double> scores = new ArrayList<Double>();
			ArrayList<Long> ids = new ArrayList<Long>();
			
			double total = 0;
			for (long friendId : friends.keySet()) {
				total += friends.get(friendId);
			}
			
			for (long friendId : friends.keySet()) {
				double val = friends.get(friendId);
				val /= total;
				friends.put(friendId, val);
			}
			
			for (long testId : combinedTest) {
				double prediction = 0;
				
				for (long friendId : friends.keySet()) {
					if (linkLikes.containsKey(testId) && linkLikes.get(testId).contains(friendId)) {
						prediction += friends.get(friendId);
					}
				}
				
				scores.add(prediction);
				ids.add(testId);
			}
			
			Object[] sorted = sort(scores, ids);
			List<Long> idLength = (List<Long>)sorted[1];
			
			int limit = boundary;
			if (idLength.size() < limit) limit = idLength.size();
		
			
			Long[] top = new Long[limit];
			for (int x = 0; x < top.length; x++) {
				top[x] = idLength.get(x);
			}
			
			
			double precision = getUserPrecision(top, userId);
			double recall = getUserRecall(top, userId, testData.get(userId));
			
			precisionRecalls.put(userId, new Double[]{precision, recall});
		}
		
		return precisionRecalls;
	}
	
	public Map<Long, Map<Long, Double>> getPredictions(Map<Long, Set<Long>> testData)
	{
		Map<Long, Map<Long, Double>> predictions = new HashMap<Long, Map<Long, Double>>();
		
		for (long userId : testData.keySet()) {
			HashMap<Long, Double> userPredictions = new HashMap<Long, Double>();
			predictions.put(userId, userPredictions);
			
			Set<Long> testLinks = testData.get(userId);
			Map<Long, Double> friends = friendships.get(userId);
			
			double total = 0;
			for (long friendId : friends.keySet()) {
				total += friends.get(friendId);
			}
			
			for (long friendId : friends.keySet()) {
				double val = friends.get(friendId);
				val /= total;
				friends.put(friendId, val);
			}
			
			for (long testId : testLinks) {
				double prediction = 0;
				
				for (long friendId : friends.keySet()) {
					if (linkLikes.containsKey(testId) && linkLikes.get(testId).contains(friendId)) {
						prediction += friends.get(friendId);
					}
				}
				
				userPredictions.put(testId, prediction);
			}
		}
		
		return predictions;
	}
	
	public void train(Map<Long, Set<Long>> trainSamples) 
	{
		try {
			if (Constants.FIW.equals(type)) {
				friendships = UserUtil.getFriendInteractionMeasure(trainSamples.keySet());
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void saveModel()
		throws SQLException
	{
		//do nothing
	}
	
	public Map<Long, Double> recommendForUser(Long userId, Set<Long> possibleLinks, int numberOfLinks)
	{
		Map<Long, Double> recommendations = new HashMap<Long, Double>();
		Map<Long, Double> friends = friendships.get(userId);
		
		double total = 0;
		for (long friendId : friends.keySet()) {
			total += friends.get(friendId);
		}
		
		for (long friendId : friends.keySet()) {
			double val = friends.get(friendId);
			val /= total;
			friends.put(friendId, val);
		}
		
		for (long linkId : possibleLinks) {	
			double score = 0;
				
			for (long friendId : friends.keySet()) {
				if (linkLikes.containsKey(linkId) && linkLikes.get(linkId).contains(friendId)) {
					score += friends.get(friendId);
				}
			}
			
			//If the recommended links are more than the max number, recommend only the highest scoring links.
			if (recommendations.size() < numberOfLinks) {
				recommendations.put(linkId, score);
			}
			else {
				//Get the lowest scoring recommended link and replace it with the current link
				//if this one has a better score.
				long lowestKey = 0;
				double lowestValue = Double.MAX_VALUE;
					
				for (long id : recommendations.keySet()) {
					if (recommendations.get(id) < lowestValue) {
						lowestKey = id;
						lowestValue = recommendations.get(id);
					}
				}
					
				if (score > lowestValue) {
					recommendations.remove(lowestKey);
					recommendations.put(linkId, score);
				}
			}
		}
		
		return recommendations;
	}
	
	public Map<Long, Map<Long, Double>> recommend(Map<Long, Set<Long>> linksToRecommend)
	{
		if (userMax == null) {
			try {
				userMax = getUserMax(linksToRecommend.keySet());
			}
			catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		Map<Long, Map<Long, Double>> recommendations = new HashMap<Long, Map<Long, Double>>();
		
		for (long userId : linksToRecommend.keySet()) {
			Set<Long> userLinks = linksToRecommend.get(userId);
			recommendations.put(userId, recommendForUser(userId, userLinks, userMax.get(userId)));
		}
		
		return recommendations;
	}
}
