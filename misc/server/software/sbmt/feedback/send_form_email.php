<?php

function clean_string($string)
{
    $bad = array("content-type","bcc:","to:","cc:","href");
    return str_replace($bad,"",$string);
}

if(isset($_POST['comments']))
{
    $comments = $_POST['comments'];
    $comments = trim($comments);

	if(strlen($comments) > 0 && strlen($comments) < 1000000)
	{
        $email_to = "eliezer.kahn@jhuapl.edu";
        $email_subject = "SBMT Feedback from website";
	    $email_from = $email_to;
		$email_message = "Form details below.\n\n";
        $email_message .= "Comments: ".clean_string($comments)."\n";

        // create email headers
        $headers = 'From: '.$email_from."\r\n".
        'Reply-To: '.$email_from."\r\n" .
        'X-Mailer: PHP/' . phpversion();

        mail($email_to, $email_subject, $email_message, $headers);
    }

    header( "Location: thankyou.html" );
}
?>
