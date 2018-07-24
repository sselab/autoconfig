# Under Construction #

AutoConfig is currently under construction (the code is need to be re-organized). We will release the clean version soon!

# AutoConfig #

AutoConfig is an automatic configuration tool that’s being developed by the members of Software Service Engineering Lab (SSELab) in [Xidian University](https://en.xidian.edu.cn/) . It can optimize producer-side throughput on distributed message systems (DMSs). AutoConfig constructs a novel comparison-based model (CBM) that is more robust that the prediction-based model (PBM) used by previous learning-based approaches. Furthermore, AutoConfig uses a weighted Latin hypercube sampling (wLHS) approach to select a set of samples that can provide a better coverage over the high-dimensional parameter space. wLHS allows AutoConfig to search for more promising configurations using the trained CBM.
We have implemented AutoConfig on the Kafka platform, and evaluated it using eight different testing scenarios deployed on a public cloud. Experimental results show that our CBM can obtain better results than that of PBM under the same random forests based model. Furthermore, AutoConfig outperforms default configurations by 215.40% on average, and five state-of-the-art configuration algorithms by 7.21%-64.56%.

For more information, see our [paper](https://github.com/sselab/autoconfig/blob/master/autoconfig.pdf). 
