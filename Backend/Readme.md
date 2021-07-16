
# Tech Stack Used :computer:

<br>
<table>
<tbody>
 <tr>
<td align="center" width="20%">
<img height=60px src="https://cdn4.iconfinder.com/data/icons/logos-3/228/android-512.png"> <br>
<span><b><center>Android</center></b></span>
</td>

<td align="center" width="20%">
<img height=60px src="https://www.onmsft.com/wp-content/uploads/2021/05/Azure-Icon.png"> <br>
<span><b><center>Azure</center></b></span>
</td>

<td align="center" width="20%">
<img height=60px src="https://cdn4.iconfinder.com/data/icons/google-i-o-2016/512/google_firebase-2-512.png"> <br>
<span><b><center>Firebase</center></b></span>
</td>
</tr>

<tr>
<td align="center" width="20%">
<img height=65px src="https://www.pngitem.com/pimgs/m/159-1595977_flask-python-logo-hd-png-download.png"> <br>
<span><b><center>Flask</center></b></span>
</td>

<td align="center" width="20%">
<img height=65px src="https://colab.research.google.com/img/colab_favicon_256px.png"> <br>
<span><b><center>Colab</center></b></span>
</td>

<td align="center" width="20%">
<img height=65px src="https://user-images.githubusercontent.com/2676579/34940598-17cc20f0-f9be-11e7-8c6d-f0190d502d64.png"> <br>
<span><b><center>Postman</center></b></span>
</td>
</tr>

<tr>
<td align="center" width="20%">
<img height=65px src="https://www.python.org/static/community_logos/python-logo.png"> <br>
<span><b><center>Python</center></b></span>
</td>

<td align="center" width="20%">
<img height=65px src="https://miro.medium.com/max/2048/1*WMf1XcyKU98dOMlNnn-Agg.png"> <br>
<span><b><center>Retrofit</center></b></span>
</td>

<td align="center" width="20%">
<img height=65px src="https://cdn.iconscout.com/icon/free/png-256/java-60-1174953.png"> <br>
<span><b><center>Java</center></b></span>
</td>
</tr>

</tbody>
</table>

## App

---
## UI/UX

---
## Text Rank model 
---

* Colab Notebook

https://colab.research.google.com/drive/1_xqcr9xeSGqSlQvtOST1OP1SpPo_nqPG?authuser=1#scrollTo=UQWnw8HmT5hA

* Based on the famous Google Page Rank Algorithm
* It finds the rank of keywords using a node based lemma graph
* Once found, sorts them and sends the words defining the paragraph in the best possible way
* It is an unsupervised model that works on a faster response time
* Data Preprocessing involves removal of stop words and Lemmetization techniques



## Deployment

---

* The model was set up as a *Flask* API.
* API was deployed on *Microsoft Azure*. on B1 Tier
* We used *Postman* for testing the API calls.
* The API runs the Text-Rank ML Model and then sends a query in Firebase(Free Tier).
* Accordng to the query, sounds are fetched from *Firebase* and returned back to our app as a result of the API.
