#  Text Rank model 


* Colab Notebook

https://colab.research.google.com/drive/1_xqcr9xeSGqSlQvtOST1OP1SpPo_nqPG?authuser=1#scrollTo=UQWnw8HmT5hA


## Tech Stack :computer:

<br>
<table>
<tbody>
 <tr>
<td align="center" width="20%">
<span><b><center>TensorFlow</center></b></span> 
<img height=60px src="https://cdn-images-1.medium.com/max/1200/1*iDQvKoz7gGHc6YXqvqWWZQ.png"> 
</td>

<td align="center" width="20%">
<span><b><center>Python</center></b></span> 
<img height=60px src="https://cdn.iconscout.com/icon/free/png-256/python-1-226045.png"> 
</td>

<td align="center" width="20%">
<span><b><center>Azure</center></b></span> 
<img height=60px src="https://www.onmsft.com/wp-content/uploads/2021/05/Azure-Icon.png"> 
</td>
</tr>

<tr>
<td align="center" width="20%">
<span><b><center>Flask</center></b></span> 
<img height=65px src="https://www.pngitem.com/pimgs/m/159-1595977_flask-python-logo-hd-png-download.png"> 
</td>

<td align="center" width="20%">
<span><b><center>Firebase</center></b></span> 
<img height=65px src="https://cdn4.iconfinder.com/data/icons/google-i-o-2016/512/google_firebase-2-512.png"> 
</td>

<td align="center" width="20%">
<span><b><center>Postman</center></b></span> 
<img height=65px src="data:image/png;base64,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"> 
</td>
</tr>

<tr>
<td align="center" width="20%">
<span><b><center>Kubernetes</center></b></span> 
<img height=65px src="https://d15shllkswkct0.cloudfront.net/wp-content/blogs.dir/1/files/2019/05/Kubernetes_New.png"> 
</td>

<td align="center" width="20%">
<span><b><center>Linux System Administration</center></b></span> 
<img height=65px src="https://upload.wikimedia.org/wikipedia/commons/a/af/Tux.png"> 
</td>



<td align="center" width="20%">
<span><b><center>Python</center></b></span> 
<img height=65px src="https://www.python.org/static/community_logos/python-logo.png"> 
</td>
</tr>

<tr>
<td align="center" width="20%">
<span><b><center>MongoDB</center></b></span> 
<img height=65px src="https://www.logolynx.com/images/logolynx/d5/d50b83324fb4fbab14cdfaf47409115b.jpeg"> 
</td>

<td align="center" width="20%">
<span><b><center>Nginx</center></b></span> 
<img height=65px src="http://www.myiconfinder.com/uploads/iconsets/256-256-cf2ed3956a3a1484f83ed20d7e987f21.png"> 
</td>

<td align="center" width="20%">
<span><b><center>SQL</center></b></span> 
<img height=65px src="https://i0.wp.com/www.complexsql.com/wp-content/uploads/2017/01/sql-logo.jpg?ssl=1"> 
</td>
</tr>

</tbody>
</table>
