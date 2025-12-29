# import pika
# import json
# import time
# import sys
# from tenacity import retry, stop_after_attempt, wait_fixed
#
# # --- ML Processing Logic ---
#
# @retry(stop=stop_after_attempt(3), wait=wait_fixed(2))
# def heavy_ml_task(data):
#     """
#     Simulates a heavy ML job.
#     Tenacity will retry this 3 times if it raises an Exception.
#     """
#     print(f" [Python] Starting ML processing for: {data}")
#     # Simulate work
#     time.sleep(2)
#     return f"Processed Result: {data} [AI-Score: 0.98]"
#
# # --- RabbitMQ Logic ---
#
# def callback(ch, method, properties, body):
#     try:
#         # 1. Handle Potential Double Serialization
#         # If Java sends a JSON string inside a JSON message, we parse twice.
#         input_data = json.loads(body)
#         if isinstance(input_data, str):
#             input_data = json.loads(input_data)
#
#         task_id = input_data.get("task_id")
#         actual_payload = input_data.get("data")
#
#         # 2. Perform the Work
#         result_text = heavy_ml_task(actual_payload)
#
#         # 3. Build the Response Payload
#         response = {
#             "task_id": task_id,
#             "status": "COMPLETED",
#             "result": result_text
#         }
#
#         # 4. Send Result Back to Java (Producer Role)
#         # We add 'application/json' so Java's Jackson converter doesn't crash
#         ch.basic_publish(
#             exchange='app.events.exchange',
#             routing_key='ml.process.finished',
#             body=json.dumps(response),
#             properties=pika.BasicProperties(
#                 content_type='application/json',
#                 delivery_mode=2  # Make message persistent
#             )
#         )
#
#         print(f" [Python] Task {task_id} successfully finished and result sent.")
#
#         # 5. Acknowledge (Safe to delete from processing queue)
#         ch.basic_ack(delivery_tag=method.delivery_tag)
#
#     except Exception as e:
#         print(f" [Python] Permanent error processing task: {e}")
#         # Reject and don't requeue -> Message goes to DLQ
#         ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
#
# def main():
#     """
#     Main loop that handles Connection Resilience.
#     If RabbitMQ goes down, this loop waits and tries to reconnect.
#     """
#     while True:
#         try:
#             print(' [*] Connecting to RabbitMQ...')
#             connection = pika.BlockingConnection(
#                 pika.ConnectionParameters(host='localhost', heartbeat=60)
#             )
#             channel = connection.channel()
#
#             # Ensure the queue exists with the correct DLQ settings
#             args = {
#                 'x-dead-letter-exchange': 'app.dlq.exchange',
#                 'x-dead-letter-routing-key': 'ml.dead'
#             }
#             channel.queue_declare(queue='ml.processing.queue', durable=True, arguments=args)
#
#             # Limit the worker to 1 message at a time (Fair Dispatch)
#             channel.basic_qos(prefetch_count=1)
#
#             channel.basic_consume(queue='ml.processing.queue', on_message_callback=callback)
#
#             print(' [*] Python Worker is ACTIVE. Waiting for tasks. To exit press CTRL+C')
#             channel.start_consuming()
#
#         except (pika.exceptions.AMQPConnectionError, pika.exceptions.ConnectionClosedByBroker) as e:
#             print(f" [!] Connection lost/failed ({e}). Retrying in 5 seconds...")
#             time.sleep(5)
#             continue  # Re-enter the 'while' loop to try connecting again
#         except KeyboardInterrupt:
#             print(" [!] Manual exit detected. Closing...")
#             break
#         except Exception as e:
#             print(f" [!] Unexpected error: {e}. Restarting in 5s...")
#             time.sleep(5)
#             continue
#
# if __name__ == '__main__':
#     main()
