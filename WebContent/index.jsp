<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <title>Child-Vehicle Tracker</title>
    <style>
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
      #map {
        height: 50%;
        width: 50%;
      }
    </style>
  </head>
  <body>
    <div id="login" style="text-align: center">
      <form name="cred">
        <label for="jid">JID:</label>
        <input type="text" id="jid" value="frankanstine@fstine.com">
        <label for="pass">Password:</label>
        <input type="password" id="pass" value="toor">
        <input type="button" id="connect" value="connect">
      </form>
    </div>
    <hr>
    <!--  <table id="log"></table> -->
    <button onClick="start()">Update Location</button>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.2.6/jquery.min.js"></script>
    <script src="strophe/strophe.js"></script>
    <script src="https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/vkbeautify/vkbeautify.0.99.00.beta.js"></script>
    <script src="js/app.js"></script>
    <p id="result"></p>
    <div id="map"></div>
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyD_vNC08HMsNGXrMiH5iZ7wA4HPlj08eI8"></script>
  
    
  </body>
</html>
