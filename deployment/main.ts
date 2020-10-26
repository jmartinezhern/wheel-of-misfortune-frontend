import { Construct } from 'constructs'
import { App, Chart } from 'cdk8s'

import { Deployment, Service, IntOrString } from './imports/k8s'

export class WheelOfMisfortuneChart extends Chart {
  constructor (scope: Construct, name: string) {
    super(scope, name)

    const label = {app: 'wheel-of-misfortune'}

    new Service(this, 'service', {
      spec: {
        selector: {
          ...label,
        },
        type: 'LoadBalancer',
        ports: [{port: 80, targetPort: IntOrString.fromNumber(3000)}]
      }
    })

    new Deployment(this, 'deployment', {
      spec: {
        replicas: 1,
        selector: {
          matchLabels: label,
        },
        template: {
          metadata: {labels: label},
          spec: {
            imagePullSecrets: [],
            containers: [{
              imagePullPolicy: 'IfNotPresent',
              name: 'wheel-of-misfortune',
              image: 'wheel-of-misfortune/app:latest',
              ports: [{containerPort: 3000}]
            }]
          },
        },
      },
    })
  }
}

const app = new App()
new WheelOfMisfortuneChart(app, 'wheel-of-misfortune')
app.synth()
