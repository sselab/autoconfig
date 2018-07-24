package lab.ride.ga;

public class Population {
	Individual[] individuals;
	
	//创建一个种群
	public Population(int populationSize, boolean initialise) {
        individuals = new Individual[populationSize]; 
        //初始化种群
        if (initialise) {
            for (int i = 0; i < populationSize; i++) {
                Individual newIndividual = new Individual();
                newIndividual.generateIndividual();
                saveIndividual(i, newIndividual);
            }
        }
    }
	
	public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }
	
	public Individual getIndividual(int index) {
        return individuals[index];
    }
	
	public Individual getFittest() {
        Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < individuals.length; i++) {
            if (fittest.getFitness() >= getIndividual(i).getFitness()) {
                fittest = getIndividual(i);
            }
        }
       
        return fittest;
    }

	public int size() {
        return individuals.length;
    }
}
